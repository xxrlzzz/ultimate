package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.List;

/**
 * 部分顺序合并，我的输出是你的输入的一部分,
 * 此时只连边不删状态
 */
public class PartSequenceMerge extends IPeaMerger {
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
        if (mDesc == null || oDesc == null) {
            throw new RuntimeException("desc not found " + mDesc + "\t" + oDesc);
        }
//        if (left.getMergedDesc()!=null) {
//            for (ReqDesc desc : left.getMergedDesc()) {
//                System.out.println(desc.getReq());
//            }
//        }
//        System.out.println(left.getDesc());
//        System.out.println(mDesc.firstCondition() + "\t" + mDesc.firstConstraint() + "\t "+ mergeT + "\t" +
//                mergeT.implies(mDesc.firstConstraint()) + "\t" + mDesc.firstConstraint().implies(mergeT));
//        System.out.println(mDesc.getReq());
        preMerge(left, right, mDesc.firstCondition(), mergeT, mergeT, oDesc.firstConstraint(), mDesc, oDesc);

        // 变量没对上
//        if (!mConsPhase.equalByState(oCondPhase)) {
        CDD stateInv = mConsPhase.getStateInvNoPhantom();
        boolean matched = false;
        for (CDD cdd : stateInv.toCNF()) {
            if (oCondPhase.getStateInvNoPhantom().implies(cdd)) {
                matched = true;
            }
        }
        for (CDD cdd : stateInv.toDNF()) {
            if (oCondPhase.getStateInvNoPhantom().implies(cdd)) {
                matched = true;
            }
        }
//        if (!matched) {
//            System.out.println("Variables: " + mConsPhase.getStateInvNoPhantom() + "\t" + oCondPhase.getStateInvNoPhantom());
//            throw new RuntimeException("部分顺序合并，变量失配");
//        }

        // 直接连边
        mConsPhase.addTransition(oCondPhase).mTarget = mergeT;
        // 先不管after

        // 处理终点
        Phase nDest = processDest(left, right);


        // 去掉被合并的状态
//        phases.remove(oCondPhase);
        inits.remove(oCondPhase);
        return makeFragment(left, right, nDest);
    }

    @Override
    protected void processDestExt(Phase nDest) {
        Transition tr = mConsPhase.getOutgoingTransition(oDest);
        if (tr != null) {
            mConsPhase.removeTransition(tr);
//            mConsPhase.addTransition(nDest, tr.getGuard(), tr.getResets());
        }
//
//        // mCondPhase 到 mDest 的边要合并。
//        mConsPhase.mergeTrans(mConsPhase, false);
//        mConsPhase.mergeTrans(nDest, false);
    }
}
