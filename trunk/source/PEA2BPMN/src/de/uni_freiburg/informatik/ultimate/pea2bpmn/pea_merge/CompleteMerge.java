package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

import java.util.Objects;
import java.util.List;

/**
 * 完全合并
 */
public class CompleteMerge extends IPeaMerger {
    @Override
    public PEAFragment merge(PEAFragment left, PEAFragment right, List<CDD> mergeTarget) {
        CDD mergeT1 = null, mergeT2 = null;
        if (mergeTarget != null) {
            assert mergeTarget.size() == 2;
            mergeT1 = mergeTarget.get(0);
            mergeT2 = mergeTarget.get(1);
        } else {
            mergeT1 = left.getDesc().firstCondition();
            mergeT2 = left.getDesc().firstConstraint();
        }
        preMerge(left, right, mergeT1, mergeT2, mergeT1, mergeT2);

        if (mCondPhase == null || oCondPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge");
        }

        // 变量没对上
        if (!mConsPhase.equalByState(oConsPhase) || !mCondPhase.equalByState(oCondPhase)) {
            throw new RuntimeException("完全合并， 变量失配");
        }

        // 因为合并后，被合并的变量就没了，因此要把所有的变迁都转移到新的上来
        // 先 migrate 一定存在的两个
        Phase afterCond = Objects.requireNonNullElse(oAfCondPhase, oConsPhase);
        PairMergerUtils.migratePhase(oCondPhase, mCondPhase, phases, afterCond);
        PairMergerUtils.migratePhase(oConsPhase, mConsPhase, phases, null);

        if (mAfCondPhase != null && oAfCondPhase != null) {
            // 两个都有 after，合并after
            CDD newClock = mAfCondPhase.getClockInvariant().and(oAfCondPhase.getClockInvariant());
            if (newClock.isEqual(CDD.FALSE)) {
                throw new RuntimeException("mergeComplete afterR 条件矛盾");
            }
            mAfCondPhase.setClockInvariant(newClock);
            // 不要的after 迁移过来
            PairMergerUtils.migratePhase(oAfCondPhase, mAfCondPhase, phases, oConsPhase);

            Transition mAfterT = mAfCondPhase.getOutgoingTransition(mConsPhase);
            Transition oAfterT = oAfCondPhase.getOutgoingTransition(oConsPhase);
            Transition mergedT = mAfterT.mergeAnd(oAfterT, null, null);
            mAfCondPhase.updateOutgoingTransition(mConsPhase, mergedT);
        } else if (mAfCondPhase != null) {
            // 我有 after，合并你的变迁条件到我的after上

            Transition mAfterT = mCondPhase.getOutgoingTransition(mAfCondPhase);
            Transition oT = oCondPhase.getOutgoingTransition(oConsPhase);
            Transition mergedT = mAfterT.mergeAnd(oT, null, null);
            mCondPhase.updateOutgoingTransition(mAfCondPhase, mergedT);
        } else if (oAfCondPhase != null) {
            // 你有 after，合并我的变迁条件到你的after 上

            Transition mT = mCondPhase.getOutgoingTransition(mConsPhase);
            Transition oAfterT = oCondPhase.getOutgoingTransition(oAfCondPhase);
            Transition mergedT = mT.mergeAnd(oAfterT, mCondPhase, oAfCondPhase);
            mCondPhase.updateOutgoingTransition(mConsPhase, mergedT);
        } else {
            // 我们都没 after，合并变迁条件

            Transition mT = mCondPhase.getOutgoingTransition(mConsPhase);
            Transition oT = oCondPhase.getOutgoingTransition(oConsPhase);
            Transition mergedT = mT.mergeAnd(oT, null, null);
            mCondPhase.updateOutgoingTransition(mConsPhase, mergedT);
        }

        // 处理终点
        Phase nDest = processDest(left, right);
        phases.remove(oCondPhase);
        phases.remove(oConsPhase);
        inits.remove(oCondPhase);

        return makeFragment(left, right, nDest);
    }

    @Override
    protected void processDestExt(Phase nDest) {
        Transition tr = mConsPhase.getOutgoingTransition(oDest);
        if (tr != null) {
            mConsPhase.removeTransition(tr);
            mConsPhase.addTransition(nDest, tr.getGuard(), tr.getResets());
        }
        Transition tr2 = mCondPhase.getOutgoingTransition(oDest);
        if (tr2 != null) {
            mCondPhase.removeTransition(tr2);
            mCondPhase.addTransition(nDest, tr2.getGuard(), tr2.getResets());
        }

        mConsPhase.mergeTrans(mConsPhase, false);
        mCondPhase.mergeTrans(mCondPhase, false);
        mConsPhase.mergeTrans(nDest, false);
        mCondPhase.mergeTrans(nDest, false);
    }
}
