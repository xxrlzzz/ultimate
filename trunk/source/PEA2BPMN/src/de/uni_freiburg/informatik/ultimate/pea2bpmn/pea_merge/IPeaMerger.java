package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.*;

public abstract class IPeaMerger {

    Set<Phase> phases = new HashSet<>();
    Set<String> clocks = new HashSet<>();
    Set<Phase> inits = new HashSet<>();
    Phase mCondPhase, oCondPhase;
    Phase mAfCondPhase, oAfCondPhase;
    Phase mConsPhase, oConsPhase;
    Phase mDest, oDest;
    ReqDesc mDesc, oDesc;

    /**
     * @param left 输入1
     * @param right 输入2
     * @param mergeTarget 根据什么合并
     * @return 合成后的片段，如果为空则合并失败
     */
    public abstract PEAFragment merge(PEAFragment left, PEAFragment right, List<CDD> mergeTarget);


    public PEAFragment merge(PEAFragment left, PEAFragment right) {
        return merge(left, right, null);
    }


    void preMerge(PEAFragment left, PEAFragment right, CDD mCondTarget, CDD mConsTarget, CDD oCondTarget, CDD oConsTarget) {
        preMerge(left, right, mCondTarget, mConsTarget, oCondTarget, oConsTarget, null, null);
    }
    /**
     * 预处理合并过程中需要的数据。
     * @param left
     * @param right
     */
    void preMerge(PEAFragment left, PEAFragment right, CDD mCondTarget, CDD mConsTarget, CDD oCondTarget, CDD oConsTarget,
                  ReqDesc mDesc, ReqDesc oDesc) {
        PairMergerUtils.mergeCommon(left, right, phases, clocks, inits);

        assert mCondTarget != null && mConsTarget != null && oCondTarget != null;
//         && oConsTarget != null
        // 根据 merge target 找到 fragment 中的 phase。
        if (mDesc == null) {
            mDesc = left.findDescByCondAndCons(mCondTarget, mConsTarget);
        }
        Phase[] findMPhases = PairMergerUtils.findPhasesInReq(mDesc, left, mCondTarget, mConsTarget);
        if (oDesc == null) {
            if (oConsTarget == null) {
                oDesc = right.findDescByCond(oCondTarget);
            } else {
                oDesc = right.findDescByCondAndCons(oCondTarget, oConsTarget);
            }
        }
        Phase[] findOPhases = PairMergerUtils.findPhasesInReq(oDesc, right, oCondTarget, oConsTarget);
        mCondPhase = findMPhases[0];
        oCondPhase = findOPhases[0];
        mAfCondPhase = findMPhases[1];
        oAfCondPhase = findOPhases[1];
        mConsPhase = findMPhases[2];
        oConsPhase = findOPhases[2];
        mDest = left.getDestPhase();
        oDest = right.getDestPhase();
        if (mCondPhase == null || oCondPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge \n"
            + mDesc.getReq() + "\n"
            + oDesc.getReq() + "\n"
            + mCondTarget + " " + mCondPhase + " ||" + oCondTarget + " " + oCondPhase + " ||"
            + mConsTarget+ " " + mConsPhase + " ||" + oConsTarget + " " + oConsPhase +"\n"
//                    + right.findDescByCondAndCons(oCondTarget, oConsTarget)
            );
        }
    }

    /**
     * 输出合并结果
     * @param left 输入1
     * @param right 输入2
     * @return 结果pea
     */
    PEAFragment makeFragment(PEAFragment left, PEAFragment right, Phase nDest) {
        PEAFragment merged = new PEAFragment(left + "-" + right, phases.toArray(new Phase[]{}),
                inits.toArray(new Phase[]{}), new ArrayList<>(clocks));

        // 收集所有的描述
        merged.addMergedDesc(left.getDesc());
        merged.addMergedDesc(right.getDesc());
        if (left.getMergedDesc() != null) {
            for (ReqDesc desc : left.getMergedDesc()) {
                merged.addMergedDesc(desc);
            }
        }
        if (right.getMergedDesc() != null) {
            for (ReqDesc desc : right.getMergedDesc()) {
                merged.addMergedDesc(desc);
            }
        }
        merged.setDestPhase(nDest);
        return merged;
    }


    protected Phase processDest(PEAFragment left, PEAFragment right) {
        Phase nDest = oDest.and(mDest, oDest.getName() + "_");
        for (Phase phase : left.getPhases()) {
            Transition tr = phase.getOutgoingTransition(mDest);
            if (tr != null) {
                phase.removeTransition(tr);
//                if (!phase.equals(mConsPhase)) {
                phase.addTransition(nDest, tr.getGuard(), tr.getResets());
//                }
            }
        }
        for (Phase phase : right.getPhases()) {
            Transition tr = phase.getOutgoingTransition(oDest);
            if (tr != null) {
                phase.removeTransition(tr);
                phase.addTransition(nDest, tr.getGuard(), tr.getResets());
            }
        }
        processDestExt(nDest);

        nDest.addSelfTrans();
        phases.add(nDest);
        phases.remove(mDest);
        phases.remove(oDest);
        return nDest;
    }

    protected void processDestExt(Phase nDest) {

    }
}
