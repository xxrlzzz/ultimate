package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.RangeDecision;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Set;

/**
 * 输入合并，考虑 conditionDuration, constraintDelay
 *
 */
public class ConditionMerge extends IPeaMerger {

    @Override
    public PEAFragment merge(PEAFragment left, PEAFragment right) {
        preMerge(left, right);

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
        PairMergerUtils.migratePhase(oConditionPhase, mConditionPhase, phases, null);

        ReqDesc leftDesc = left.getDesc(), rightDesc = right.getDesc();
        boolean mhasConsDl = left.getDesc().constraintDelay != CDD.TRUE,
                ohasConsDl = right.getDesc().constraintDelay != CDD.TRUE;

        // merge delay.
        if (mhasConsDl && ohasConsDl) {
            // 咱们都有delay 合并delay

            // 如果 R duration 不同，则没办法合并
            if (!leftDesc.conditionDuration.isEqual(rightDesc.conditionDuration)) {
                System.out.printf("R duration different between %s and %s, unable to merge\n", left.getName(), right.getName());
                return null;
            }

            Transition moT = mConditionPhase.getOutgoingTransition(mAfterCondPhase),
                    ooT = oConditionPhase.getOutgoingTransition(oAfterCondPhase);
            if (mAfterCondPhase == null || oAfterCondPhase == null || moT == null || ooT == null) {
                throw new RuntimeException("not found after condition phase when merge");
            }
            // 有两种做法：把长的delay拆成两阶段/把小的升级成大的
            // 这里是吧小的升级成大的
            int mV = ((RangeDecision) leftDesc.constraintDelay.getDecision()).getVal(0);
            int oV = ((RangeDecision) rightDesc.constraintDelay.getDecision()).getVal(0);

            // 两个 delay 都应该是 <=
            CDD bothDelay = leftDesc.constraintDelay.and(rightDesc.constraintDelay);
            if (bothDelay.isEqual(leftDesc.constraintDelay)) {
                // 我更小，迁移到大的
                PairMergerUtils.migratePhase(mAfterCondPhase, oAfterCondPhase, phases, null);
                moT.putClockWriter(moT.getGuard().getDecision().getVar(), oV - mV); // diff of two phase
            } else if (bothDelay.isEqual(rightDesc.constraintDelay)) {
                // oth has smaller range
                PairMergerUtils.migratePhase(oAfterCondPhase, mAfterCondPhase, phases, null);
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

        return makeFragment(left.getName() + "-" + right.getName());
    }


    private static void borrowAsAfterPhase(Phase source, Phase afterCondPhase, Phase consNoAfter, Phase consWithAfter, Set<Phase> phases) {
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
            PairMergerUtils.assert_no_incoming = true;
            // NOTE: 这里可能导致 afterCondPhase consNoAfter 状态不变式不同
            afterCondPhase.setStateInvariant(consNoAfter.getStateInvariant().and(afterCondPhase.getStateInvariant()));
            PairMergerUtils.migratePhase(afterCondPhase, consNoAfter, phases, consWithAfter);
            PairMergerUtils.assert_no_incoming = false;
        }

        // 你的 dest 做我的delay
        Transition t = consNoAfter.addTransition(consWithAfter, afterCondPhase.getClockInvariant(), new String[]{});
        // 两个结果并发启动
        t.isParallel = true;
    }

}
