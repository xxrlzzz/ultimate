package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.List;

public class ScopeSequenceMerge extends IPeaMerger {
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
        oDesc = right.findDescByScope(mergeT);
//        if (left.getMergedDesc() != null) {
//            for (ReqDesc desc : left.getMergedDesc()) {
//                System.out.println(desc.getReq());
//            }
//            System.out.println(mergeT);
//        }

        if (mDesc == null || oDesc == null) {
            throw new RuntimeException("desc not found " + mDesc + "\t" + oDesc);
        }
        preMerge(left, right, mDesc.firstCondition(), mergeT, oDesc.firstCondition(), null);
        // TODO 检查变量匹配

        // 处理终点
        Phase nDest = processDest(left, right);

        mConsPhase.addTransition(oCondPhase).mTarget = mergeT;
        inits.remove(oCondPhase);
        return makeFragment(left, right, nDest);
    }
}
