package de.uni_freiburg.informatik.ultimate.pea2bpmn.req;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;

import java.util.*;
import java.util.function.BiConsumer;


public class PEAFragment extends PhaseEventAutomata {
    ReqDesc mDesc;
    ArrayList<Phase> mOut;
    HashMap<Phase, CDD> mOutCondition;

    public PEAFragment(final String name, final Phase[] phases, final Phase[] init) {
        this(name, phases, init, new ArrayList<String>());
    }

    public PEAFragment(final String name, final Phase[] phases, final Phase[] init, final List<String> clocks) {
        this(name, phases, init, clocks, null, null);
    }

    public PEAFragment(final String name, final Phase[] phases, final Phase[] init, final List<String> clocks,
                       final Map<String, String> variables, final List<String> declarations) {
        this(name, phases, init, clocks, variables, null, declarations);
    }

    /**
     * @param clocks
     * @param declarations
     * @param init
     * @param name
     * @param phases
     * @param variables
     */
    public PEAFragment(final String name, final Phase[] phases, final Phase[] init, final List<String> clocks,
                       final Map<String, String> variables, final Set<String> events, final List<String> declarations) {
        super(name, phases, init, clocks, variables, events, declarations);
        mOut = new ArrayList<Phase>();
        mOutCondition = new HashMap<Phase, CDD>();
    }


    private static Phase[] findPhasesInReq(ReqDesc desc, Phase[] phases) {
        Phase condition = null, afterCond = null, constraint = null;
        for (Phase phase : phases) {
//            if (desc.firstCondition() == null || desc.firstCondition().getDecision() == null) {
//                throw new RuntimeException("empty desc first conditon " + desc.getReq());
//            }
//            if (phase.getStateInvariant() == null || phase.getStateInvariant().getDecision() == null) {
//                throw new RuntimeException("empty phase: " + phase + "\t"+ phase.getStateInvariant());
//            }
            if (phase.getStateInvariant().isEqual(desc.firstCondition())) {
                condition = phase;
            }
            // TODO: fix after cond find
            if (phase.getName().contains("After")) {
                afterCond = phase;
            }
            if (phase.getStateInvariant().isEqual(desc.firstConstraint())) {
                constraint = phase;
            }
        }
        return new Phase[]{condition, afterCond, constraint};
    }

    public void mergeCommon(PEAFragment oth, Set<Phase> phases, Set<String> clocks, Set<Phase> inits) {
        phases.addAll(List.of(getPhases()));
        phases.addAll(List.of(oth.getPhases()));

        clocks.addAll(getClocks());
        clocks.addAll(oth.getClocks());

        inits.addAll(List.of(getInit()));
        inits.addAll(List.of(oth.getInit()));
    }

    // check there has any incoming transition
    private static boolean assert_no_incoming = false;
    /**
     * merge phase from source to target
     * 1. outgoing transitions
     * 2. incoming transitions
     * 3. delete the phase
     * @param source
     * @param target
     */
    private void migratePhase(Phase source, Phase target, Set<Phase> phases, Phase skipDest) {
        if (!source.getStateInvariant().isEqual(target.getStateInvariant())) {
            throw new RuntimeException("migrate phase but not same state invariant\t" + source.getStateInvariant() + "\t" + target.getStateInvariant());
        }

        // outgoing
        for (Transition transition : source.getTransitions()) {
            if (skipDest != null && transition.getDest().compareTo(skipDest) == 0) {
                continue;
            }
            Transition nt = target.addTransition(transition.getDest(), transition.getGuard(), transition.getResets());
            transition.getClockWriter().forEach((c, t) -> {
                // 先不考虑 clock writer 冲突的情况
                nt.putClockWriter(c, t);
            });
        }
        // incoming
        for (Phase phase : phases) {
            Transition transition = phase.getOutgoingTransition(source);
            if (transition == null) continue;
            if (assert_no_incoming) {
                assert_no_incoming = false;
                throw new RuntimeException("expect no incoming transition but has");
            }
            Transition maybeT = phase.getOutgoingTransition(target);
            if (maybeT != null) {
                // 已经有到 target 的边了，这里选择升级老边
                maybeT.setGuard(maybeT.getGuard().and(transition.getGuard()));
                // TODO: merge transition
            }
            Transition nt = phase.addTransition(transition.getDest(), transition.getGuard(), transition.getResets());
            transition.getClockWriter().forEach((c, t) -> {
                nt.putClockWriter(c, t);
            });
        }
        phases.remove(source);
    }

    private void borrowAsAfterPhase(Phase source, Phase afterCondPhase, Phase consNoAfter, Phase consWithAfter, Set<Phase> phases) {
        Transition moT = source.getOutgoingTransition(afterCondPhase),
                ooT = source.getOutgoingTransition(consNoAfter);
        if (afterCondPhase == null || moT == null || ooT == null) {
            throw new RuntimeException("not found after condition phase when merge");
        }
        // 跳 consPhaseNoAfter 时 重置 ARt
        ooT.addReset(afterCondPhase.getClockInvariant().getDecision().getVar());
        if (ooT.getGuard().isEqual(CDD.TRUE)) {
            // 没有条件，直接当做 AfterR, 老 AfterR 不要了
            source.removeTransition(moT);
            assert_no_incoming = true;
            // NOTE: 这里可能导致 afterCondPhase consNoAfter 状态不变式不同
            afterCondPhase.setStateInvariant(consNoAfter.getStateInvariant().and(afterCondPhase.getStateInvariant()));
            migratePhase(afterCondPhase, consNoAfter, phases, consWithAfter);
            assert_no_incoming = false;
        }

        // 你的 dest 做我的delay
        Transition t = consNoAfter.addTransition(consWithAfter, afterCondPhase.getClockInvariant(), new String[]{});
        // 两个结果并发启动
        t.isParallel = true;
    }

    /**
     * 输入合并，考虑 conditionDuration, constraintDelay
     * @param oth
     * @return 合成后的片段，如果为空则合并失败
     */
    public PEAFragment mergeCondition(PEAFragment oth) {
        Set<Phase> phases = new HashSet<Phase>();
        Set<String> clocks = new HashSet<String>();
        Set<Phase> inits = new HashSet<Phase>();
        mergeCommon(oth, phases, clocks, inits);

        // find the phase in two fragment.
        Phase[] findMPhases = findPhasesInReq(getDesc(), getPhases());
        Phase[] findOPhases = findPhasesInReq(oth.getDesc(), oth.getPhases());
        Phase mConditionPhase = findMPhases[0], oConditionPhase = findOPhases[0];
        Phase mAfterCondPhase = findMPhases[1], oAfterCondPhase = findOPhases[1];
        Phase mConsPhase = findMPhases[2], oConsPhase = findOPhases[2];

        if (mConditionPhase == null || oConditionPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge");
        }

        // 变量没对上
        if (!mConditionPhase.equalByState(oConditionPhase)) {
            throw new RuntimeException("输入合并， 变量失配");
        }

        // migrate condition phase
        // 条件的时钟不变量取交集
        CDD newClock = mConditionPhase.getClockInvariant().and(oConditionPhase.getClockInvariant());
        mConditionPhase.setClockInvariant(newClock);
        migratePhase(oConditionPhase, mConditionPhase, phases, null);

        boolean mhasConsDl = mDesc.constraintDelay != CDD.TRUE,
                ohasConsDl = oth.getDesc().constraintDelay != CDD.TRUE;

        // merge delay.
        if (mhasConsDl && ohasConsDl) {
            // 咱们都有delay 合并delay

            // 如果 R duration 不同，则没办法合并
            if (!mDesc.conditionDuration.isEqual(oth.mDesc.conditionDuration)) {
                System.out.printf("R duration different between %s and %s, unable to merge\n", getName(), oth.getName());
                return null;
            }

            Transition moT = mConditionPhase.getOutgoingTransition(mAfterCondPhase),
                    ooT = oConditionPhase.getOutgoingTransition(oAfterCondPhase);
            if (mAfterCondPhase == null || oAfterCondPhase == null || moT == null || ooT == null) {
                throw new RuntimeException("not found after condition phase when merge");
            }
            // 有两种做法：把长的delay拆成两阶段/把小的升级成大的
            // 这里是吧小的升级成大的
            int mV = ((RangeDecision) mDesc.constraintDelay.getDecision()).getVal(0);
            int oV = ((RangeDecision) oth.mDesc.constraintDelay.getDecision()).getVal(0);

            // 两个 delay 都应该是 <=
            CDD bothDelay = mDesc.constraintDelay.and(oth.mDesc.constraintDelay);
            if (bothDelay.isEqual(mDesc.constraintDelay)) {
                // 我更小，迁移到大的
                migratePhase(mAfterCondPhase, oAfterCondPhase, phases, null);
                moT.putClockWriter(moT.getGuard().getDecision().getVar(), oV - mV); // diff of two phase
            } else if (bothDelay.isEqual(oth.mDesc.constraintDelay)) {
                // oth has smaller range
                migratePhase(oAfterCondPhase, mAfterCondPhase, phases, null);
                ooT.putClockWriter(moT.getGuard().getDecision().getVar(), mV - oV); // diff of two phase
            } else {
                throw new RuntimeException("unable to merge due to delay guard");
            }
        } else if (mhasConsDl) {
            // 我有delay 你没有, 你的dest 做我的 delay
            borrowAsAfterPhase(mConditionPhase, mAfterCondPhase, oConsPhase, mConsPhase, phases);
        } else if (ohasConsDl) {
            // 你有 delay 我没有, 我的dest 做你的 delay
            borrowAsAfterPhase(mConditionPhase, oAfterCondPhase, mConsPhase, oConsPhase, phases);
        }

        phases.remove(oConditionPhase);
        // 要不要删其他的？

        return new PEAFragment(getName() + "-" + oth.getName(), phases.toArray(new Phase[]{}),
                inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
    }

    /**
     * 约束合并
     * @param oth
     * @return
     */
    public PEAFragment mergeConstraint(PEAFragment oth) {
        Set<Phase> phases = new HashSet<Phase>();
        Set<String> clocks = new HashSet<String>();
        Set<Phase> inits = new HashSet<Phase>();
        mergeCommon(oth, phases, clocks, inits);

        // find the phase in two fragment.
        Phase[] findMPhases = findPhasesInReq(getDesc(), getPhases());
        Phase[] findOPhases = findPhasesInReq(oth.getDesc(), oth.getPhases());
        Phase mConditionPhase = findMPhases[0], oConditionPhase = findOPhases[0];
        Phase mAfterCondPhase = findMPhases[1], oAfterCondPhase = findOPhases[1];
        Phase mConsPhase = findMPhases[2], oConsPhase = findOPhases[2];

        if (mConditionPhase == null || oConditionPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge");
        }

        // 变量没对上
        if (!mConsPhase.equalByState(oConsPhase)) {
            throw new RuntimeException("约束合并， 变量失配");
        }

        // 需要升级的时钟量， 0 为不升级， 负数为 me 升级， 正数为 oth 升级。
        int upgradeDiff = 0;

        if (!oConsPhase.getClockInvariant().isEqual(mConsPhase.getClockInvariant())) {
            // 两个片段中时钟约束不一样，要升级一个
            if (oConsPhase.getClockInvariant().and(mConsPhase.getClockInvariant()).isEqual(CDD.FALSE)) {
                // 互斥 无法合并
                System.out.println("mergeConstraint 结果的时钟约束互斥");
                return null;
            }
            // 假设他们都是 >=
            // 吧小的升级成大的
            int mV = 0, oV = 0;
            if (!mDesc.constraintDuration.isEqual(CDD.TRUE)) {
                mV = ((RangeDecision) mDesc.constraintDuration.getDecision()).getVal(0);
            }
            if (!oth.mDesc.constraintDuration.isEqual(CDD.TRUE)) {
                oV = ((RangeDecision) oth.mDesc.constraintDuration.getDecision()).getVal(0);
            }
            if (mV == oV) {
                throw new RuntimeException("mergeConstraint 相同的时钟约束，但判断失败");
            }
            upgradeDiff = mV - oV;
        }
        // migrate condition phase
        migratePhase(oConsPhase, mConsPhase, phases, null);

        // 处理一下升级
        if (upgradeDiff != 0) {
            // can be after cond or cond.
            Phase mPreConsPhase, oPreConsPhase;
            String clock = mConsPhase.getClockInvariant().getDecision().getVar();
            mPreConsPhase = Objects.requireNonNullElse(mAfterCondPhase, mConditionPhase);
            oPreConsPhase = Objects.requireNonNullElse(oAfterCondPhase, oConditionPhase);
            Transition moT = mPreConsPhase.getOutgoingTransition(mConsPhase),
                    ooT = oPreConsPhase.getOutgoingTransition(mConsPhase);
            if (moT == null || ooT == null) {
                throw new RuntimeException("mergeConstraint 找不到条件到结果的变迁");
            }
            if (upgradeDiff > 0) {
                moT.putClockWriter(clock, upgradeDiff);
            } else {
                ooT.putClockWriter(clock, -upgradeDiff);
            }
        }

        phases.remove(oConsPhase);

        return new PEAFragment(getName() + "-" + oth.getName(), phases.toArray(new Phase[]{}),
                inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
    }

    /**
     * 完全合并
     * @param oth
     * @return
     */
    public PEAFragment mergeComplete(PEAFragment oth) {
        Set<Phase> phases = new HashSet<>();
        Set<String> clocks = new HashSet<>();
        Set<Phase> inits = new HashSet<>();
        mergeCommon(oth, phases, clocks, inits);

        // find the phase in two fragment.
        Phase[] findMPhases = findPhasesInReq(getDesc(), getPhases());
        Phase[] findOPhases = findPhasesInReq(oth.getDesc(), oth.getPhases());
        Phase mConditionPhase = findMPhases[0], oConditionPhase = findOPhases[0];
        Phase mAfterCondPhase = findMPhases[1], oAfterCondPhase = findOPhases[1];
        Phase mConsPhase = findMPhases[2], oConsPhase = findOPhases[2];

        if (mConditionPhase == null || oConditionPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge");
        }

        // 变量没对上
        if (!mConsPhase.equalByState(oConsPhase) || !mConditionPhase.equalByState(oConditionPhase)) {
            throw new RuntimeException("完全合并， 变量失配");
        }

        // 因为合并后，被合并的变量就没了，因此要把所有的变迁都转移到新的上来
        // 先 migrate 一定存在的两个
        Phase afterCond = Objects.requireNonNullElse(oAfterCondPhase, oConsPhase);
        migratePhase(oConditionPhase, mConditionPhase, phases, afterCond);
        migratePhase(oConsPhase, mConsPhase, phases, null);

        if (mAfterCondPhase != null && oAfterCondPhase != null) {
            // 两个都有 after，合并after
            CDD newClock = mAfterCondPhase.getClockInvariant().and(oAfterCondPhase.getClockInvariant());
            if (newClock.isEqual(CDD.FALSE)) {
                throw new RuntimeException("mergeComplete afterR 条件矛盾");
            }
            mAfterCondPhase.setClockInvariant(newClock);
            // 不要的after 迁移过来
            migratePhase(oAfterCondPhase, mAfterCondPhase, phases, oConsPhase);

            Transition mAfterT = mAfterCondPhase.getOutgoingTransition(mConsPhase);
            Transition oAfterT = oAfterCondPhase.getOutgoingTransition(oConsPhase);
            Transition mergedT = mAfterT.mergeAnd(oAfterT, null, null);
            mAfterCondPhase.updateOutgoingTransition(mConsPhase, mergedT);
        } else if (mAfterCondPhase != null) {
            // 我有 after，合并你的变迁条件到我的after上

            Transition mAfterT = mConditionPhase.getOutgoingTransition(mAfterCondPhase);
            Transition oT = oConditionPhase.getOutgoingTransition(oConsPhase);
            Transition mergedT = mAfterT.mergeAnd(oT, null, null);
            mConditionPhase.updateOutgoingTransition(mAfterCondPhase, mergedT);
        } else if (oAfterCondPhase != null) {
            // 你有 after，合并我的变迁条件到你的after 上

            Transition mT = mConditionPhase.getOutgoingTransition(mConsPhase);
            Transition oAfterT = oConditionPhase.getOutgoingTransition(oAfterCondPhase);
            Transition mergedT = mT.mergeAnd(oAfterT, mConditionPhase, oAfterCondPhase);
            mConditionPhase.updateOutgoingTransition(mConsPhase, mergedT);
        } else {
            // 我们都没 after，合并变迁条件

            Transition mT = mConditionPhase.getOutgoingTransition(mConsPhase);
            Transition oT = oConditionPhase.getOutgoingTransition(oConsPhase);
            Transition mergedT = mT.mergeAnd(oT, null, null);
            mConditionPhase.updateOutgoingTransition(mConsPhase, mergedT);
        }

        phases.remove(oConditionPhase);
        phases.remove(oConsPhase);

        return new PEAFragment(getName() + "-" + oth.getName(), phases.toArray(new Phase[]{}),
                inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
    }

    /**
     * 顺序合并，我的输出是你的输入
     * @param oth
     * @return
     */
    public PEAFragment mergeSequence(PEAFragment oth) {
        Set<Phase> phases = new HashSet<>();
        Set<String> clocks = new HashSet<>();
        Set<Phase> inits = new HashSet<>();
        mergeCommon(oth, phases, clocks, inits);

        // find the phase in two fragment.
        Phase[] findMPhases = findPhasesInReq(getDesc(), getPhases());
        Phase[] findOPhases = findPhasesInReq(oth.getDesc(), oth.getPhases());
        Phase mConditionPhase = findMPhases[0], oConditionPhase = findOPhases[0];
        Phase mAfterCondPhase = findMPhases[1], oAfterCondPhase = findOPhases[1];
        Phase mConsPhase = findMPhases[2], oConsPhase = findOPhases[2];

        if (mConditionPhase == null || oConditionPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge");
        }

        // 变量没对上
        if (!mConsPhase.equalByState(oConditionPhase)) {
            throw new RuntimeException("顺序合并， 变量失配");
        }

        // 直接迁移就可以
        migratePhase(oConditionPhase, mConsPhase, phases, null);

        phases.remove(oConditionPhase);
        return new PEAFragment(getName() + "-" + oth.getName(), phases.toArray(new Phase[]{}),
                inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
    }

    public void addOut(Phase out, CDD condition) {
        mOut.add(out);
        // 不要 outcondition
//        mOutCondition.put(out, condition);
    }

    public void setDesc(ReqDesc desc) {
        mDesc = desc;
    }

    public ReqDesc getDesc() {
        return mDesc;
    }
}
