package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

import java.util.Objects;

/**
 * 完全合并
 */
public class CompleteMerge extends IPeaMerger {
    @Override
    public PEAFragment merge(PEAFragment left, PEAFragment right) {
        preMerge(left, right);

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
        PairMergerUtils.migratePhase(oConditionPhase, mConditionPhase, phases, afterCond);
        PairMergerUtils.migratePhase(oConsPhase, mConsPhase, phases, null);

        if (mAfterCondPhase != null && oAfterCondPhase != null) {
            // 两个都有 after，合并after
            CDD newClock = mAfterCondPhase.getClockInvariant().and(oAfterCondPhase.getClockInvariant());
            if (newClock.isEqual(CDD.FALSE)) {
                throw new RuntimeException("mergeComplete afterR 条件矛盾");
            }
            mAfterCondPhase.setClockInvariant(newClock);
            // 不要的after 迁移过来
            PairMergerUtils.migratePhase(oAfterCondPhase, mAfterCondPhase, phases, oConsPhase);

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
        inits.remove(oConditionPhase);

        return makeFragment(left, right);
    }
}
