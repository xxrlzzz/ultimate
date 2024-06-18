package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.RangeDecision;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Objects;
import java.util.List;

/**
 * 约束合并
 */
public class ConstraintMerge extends IPeaMerger {
    @Override
    public PEAFragment merge(PEAFragment left, PEAFragment right, List<CDD> mergeTarget) {
        CDD mergeT = null;
        if (mergeTarget != null) {
            assert mergeTarget.size() == 1;
            mergeT = mergeTarget.get(0);
        } else {
            mergeT = left.getDesc().firstConstraint();
        }
        mDesc = left.findDescByCons(mergeT);
        oDesc = right.findDescByCons(mergeT);
        preMerge(left, right, mDesc.firstCondition(), mergeT, oDesc.firstCondition(), mergeT);
        if (mCondPhase == null || oCondPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge");
        }

        // 变量没对上
        if (!mConsPhase.getStateInvNoPhantom().isEqual(oConsPhase.getStateInvNoPhantom())) {
            throw new RuntimeException("约束合并， 变量失配" + mCondPhase.getStateInvariant() + "\t" + oCondPhase.getStateInvariant());
        }
//        mConsPhase.setStateInvariant(mConsPhase.getStateInvariant().and(oConsPhase.getStateInvariant()));

        oConsPhase.migratePhantom(mConsPhase);
        PairMergerUtils.migratePhase(oConsPhase, mConsPhase, phases, null);

        boolean mHasConsDr = left.getDesc().constraintDuration != CDD.TRUE,
                oHasConsDl = right.getDesc().constraintDuration != CDD.TRUE;

        if (mHasConsDr || oHasConsDl) {
            // TODO 有一个有时长限制，就要准备升级

            // 两个时长一致就不用升级，

            // 否则两个做差值， 对小的升级
        }
        // 处理终点
        Phase nDest = processDest(left, right);
        phases.remove(oConsPhase);

        return makeFragment(left, right, nDest);
    }

    private void oldUpgradeClock(PEAFragment left, PEAFragment right) {
        ReqDesc leftDesc = left.getDesc(), rightDesc = right.getDesc();
        // 需要升级的时钟量， 0 为不升级， 负数为 me 升级， 正数为 oth 升级。
        int upgradeDiff = 0;

        // 约束不在位置上，在位置向后迁移的边上，难办。
        if (!oConsPhase.getClockInvariant().isEqual(mConsPhase.getClockInvariant())) {
            // 两个片段中时钟约束不一样，要升级一个
            if (oConsPhase.getClockInvariant().and(mConsPhase.getClockInvariant()).isEqual(CDD.FALSE)) {
                // 互斥 无法合并
                System.out.println("mergeConstraint 结果的时钟约束互斥");
                return;
            }
            // 假设他们都是 >=
            // 吧小的升级成大的
            int mV = 0, oV = 0;
            if (!leftDesc.constraintDuration.isEqual(CDD.TRUE)) {
                mV = ((RangeDecision) leftDesc.constraintDuration.getDecision()).getVal(0);
            }
            if (!rightDesc.constraintDuration.isEqual(CDD.TRUE)) {
                oV = ((RangeDecision) rightDesc.constraintDuration.getDecision()).getVal(0);
            }
            if (mV == oV) {
                throw new RuntimeException("mergeConstraint 相同的时钟约束，但判断失败");
            }
            upgradeDiff = mV - oV;
            System.out.println("要升级 " + upgradeDiff);
        }
        // migrate condition phase
        PairMergerUtils.migratePhase(oConsPhase, mConsPhase, phases, null);

        // 处理一下升级
        if (upgradeDiff != 0) {
            // can be after cond or cond.
            Phase mPreConsPhase, oPreConsPhase;
//            if (mConsPhase.getClockInvariant().isEqual(CDD.TRUE)) {
//                // 我没有constraint
//                // TODO 为我加上 constraint delay
//                return null;
//            }
            String clock = mConsPhase.getClockInvariant().getDecision().getVar();
            mPreConsPhase = Objects.requireNonNullElse(mAfCondPhase, mCondPhase);
            oPreConsPhase = Objects.requireNonNullElse(oAfCondPhase, oCondPhase);
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

    }

    @Override
    protected void processDestExt(Phase nDest) {
        Transition tr = mConsPhase.getOutgoingTransition(oDest);
        if (tr != null) {
            mConsPhase.removeTransition(tr);
            mConsPhase.addTransition(nDest, tr.getGuard(), tr.getResets());
        }
        // 合并子变迁
        mConsPhase.mergeTrans(mConsPhase, false);
        // 合并到终点的变迁
        mConsPhase.mergeTrans(nDest, false);
    }
}
