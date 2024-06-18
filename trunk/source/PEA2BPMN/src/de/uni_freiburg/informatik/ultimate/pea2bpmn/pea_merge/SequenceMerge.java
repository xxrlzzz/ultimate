package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

import java.util.List;

/**
 * 顺序合并，我的输出是你的输入
 */
public class SequenceMerge extends IPeaMerger {
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
        oDesc = right.findDescByCond(mergeT);
        System.out.println(mDesc.getReq() + "\t" + mDesc.firstConstraint() + "\t" + mDesc.firstCondition());
        preMerge(left, right, mDesc.firstCondition(), mergeT, mergeT, oDesc.firstConstraint());

        // 变量没对上
        if (!mConsPhase.equalByState(oCondPhase)) {
//        if (!mConsPhase.getStateInvariant().implies(oCondPhase.getStateInvariant()) &&
//            !oCondPhase.getStateInvariant().implies(mConsPhase.getStateInvariant())) {
            System.out.println(mConsPhase.getStateInvariant() + "\t" + oCondPhase.getStateInvariant());
            throw new RuntimeException("顺序合并，变量失配");
        }

        // 先设置边的重置，为进入重置
        Transition entryTr = mCondPhase.getOutgoingTransition(mConsPhase);
//        System.out.println(mCondPhase.getStateInvariant() + "\t" + mAfCondPhase);
//        List<Transition> transitions = mCondPhase.getTransitions();
//        for (Transition transition : transitions) {
//            System.out.println(transition.getDest().getStateInvariant());
//        }
        if (mAfCondPhase != null) {
            entryTr = mAfCondPhase.getOutgoingTransition(mConsPhase);
        }
        if (entryTr == null) {
            throw new RuntimeException("cond 到 cons 没有边了?");
        }
        if (right.getEntryReset() != null) {
            entryTr.addReset(right.getEntryReset());
        }
        // 直接迁移
        PairMergerUtils.migratePhase(oCondPhase, mConsPhase, phases, mDest);

        /**
         * 去掉 phantom 的条件 (oth上)
         * 1. scope 为 after
         * TODO: 现在默认有 After 就行，不管 After 是什么
         * 2. 变迁是 eventually
         */
        if (oDesc.getReq().getScope().type() != SrParseScope.ScopeType.After &&
            !oDesc.getReq().getName().equals("ResponsePattern")) {
            //TODO: 给 oCond后面的加 phantom，现在只给oCons加了
            mConsPhase.migratePhantom(oConsPhase);
        }

        // 处理终点
        Phase nDest = processDest(left, right);

        // 去掉被合并的状态
        phases.remove(oCondPhase);
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

        // mCondPhase 到 mDest 的边要合并。
        mConsPhase.mergeTrans(mConsPhase, false);
        mConsPhase.mergeTrans(nDest, false);
    }
}
