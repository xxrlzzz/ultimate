package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.RangeDecision;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Objects;

/**
 * 约束合并
 */
public class ConstraintMerge extends IPeaMerger {
    @Override
    public PEAFragment merge(PEAFragment left, PEAFragment right) {
        preMerge(left, right);

        if (mConditionPhase == null || oConditionPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge");
        }

        // 变量没对上
        if (!mConsPhase.equalByState(oConsPhase)) {
            throw new RuntimeException("约束合并， 变量失配");
        }

        ReqDesc leftDesc = left.getDesc(), rightDesc = right.getDesc();
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
        }
        // migrate condition phase
        PairMergerUtils.migratePhase(oConsPhase, mConsPhase, phases, null);

        // 处理一下升级
        if (upgradeDiff != 0) {
            // can be after cond or cond.
            Phase mPreConsPhase, oPreConsPhase;
            if (mConsPhase.getClockInvariant().isEqual(CDD.TRUE)) {
                // 我没有constraint
                // TODO 为我加上 constraint delay
                return null;
            }
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

        return makeFragment(left, right);
    }
}
