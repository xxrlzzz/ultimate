package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.*;

public class ResponseBoundL1PeaImpl extends AbsPeaImpl<ResponseBoundL1Pattern> {
//    private final ResponseBoundL1Pattern mReq;
    // ResponseBoundL1Pattern or EdgeResponseBoundL1Pattern

    final int c1;

    public ResponseBoundL1PeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(1);
        mClocks.add(R.toUppaalString() + "c");
        c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();
    }

    @Override
    public PEAFragment generate() {
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);
        final CDD R_S = R.and(S);

        String rClock = mClocks.get(0);
        CDD condDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
        CDD n_condDr = condDr.negate();
        Phase pr = new Phase(id + "_st1", R);
        Phase ps = new Phase(id + "_st2", S);
        Phase prs = new Phase(id + "_st3", R_S);
        Phase p_true = new Phase(id + "_st4", R.negate().and(S.negate()));

        pr.addTransition(pr, n_condDr);
        ps.addSelfTrans();
        prs.addSelfTrans();

        ps.addTransition(p_true);
        prs.addTransition(p_true);
        pr.addTransition(p_true);
        p_true.addSelfTrans();

        pr.addTransition(ps, condDr);
        pr.addTransition(prs, condDr);
        prs.addTransition(ps, condDr);
        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, ps, prs, p_true}, new Phase[]{pr},
                mClocks);
        pea.setDestPhase(p_true);
        pea.setEntryReset(rClock);
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), condDr, CDD.TRUE, CDD.TRUE));
        return pea;
    }

    @Override
    CDD condDr() {
        String rClock = mClocks.get(0);
        return RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
    }

    @Override
    void handleOnePair(CDD R, CDD S, int i, int j, HashMap<String, Phase> phases, ArrayList<Phase> init, Phase p_true) {
        String rName = id + "_st1_" + i, sName = id + "_st2_" + j;
        Phase pr = phases.get(rName);
        Phase ps = phases.get(sName);
        CDD condDr = condDr();
        CDD n_condDr = condDr.negate();
        if (pr == null) {
            pr = new Phase(rName, R);
            pr.addTransition(pr, n_condDr);
            pr.addTransition(p_true, n_condDr);
            phases.put(rName, pr);
            init.add(pr);
        }
        if (ps == null) {
            ps = new Phase(sName, S);
            ps.addSelfTrans();
            ps.addTransition(p_true);
            phases.put(sName, ps);
        }
        ps.setStateInvariant(ps.getStateInvariant().and(R.operator("p")));
        ps.addPhantom(R, false);

        pr.addTransition(ps, condDr);
    }

    @Override
    public PEAFragment gen4merge() {
        return gen4mergeCommon();
//        final CDD R = mReq.getCdds().get(1);
//        final CDD S = mReq.getCdds().get(0);
//
//        String rClock = mClocks.get(0);
//        CDD condDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
//        CDD n_condDr = condDr.negate();
//        Phase pr = new Phase(id + "_st1", R);
//        Phase ps = new Phase(id + "_st2", R.operator("p").and(S));
//        ps.addPhantom(R, false);
//
//        Phase p_true = new Phase(id + "_st3", R.negate().and(S.negate()));
//
//        pr.addTransition(pr, n_condDr);
//        ps.addSelfTrans();
//
//        ps.addTransition(p_true);
//        pr.addTransition(p_true, n_condDr);
//        p_true.addSelfTrans();
//
//        pr.addTransition(ps, condDr);
//        PEAFragment pea = new PEAFragment(getPEAName(), new Phase[]{pr, ps, p_true}, new Phase[]{pr},
//                mClocks);
//        pea.setDestPhase(p_true);
//        pea.setEntryReset(rClock);
//        Set<Phase> phase = new HashSet<>();
//        Collections.addAll(phase, pea.getPhases());
//        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), condDr, CDD.TRUE, CDD.TRUE, phase));
//        return pea;
    }
}
