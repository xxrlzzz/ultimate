package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponsePattern;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.*;

public class ResponsePeaImpl extends AbsPeaImpl<ResponsePattern> {
    public ResponsePeaImpl(PatternType<?> req) {
        super(req);
    }

    @Override
    public PEAFragment generate() {
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);

        Phase pr = new Phase(id + "_st1", R);
        Phase ps = new Phase(id + "_st2", S);
        Phase p_true = new Phase(id + "_st3", R.negate().and(S.negate()));

        pr.addSelfTrans();
        ps.addSelfTrans();
        ps.addTransition(p_true);
        p_true.addSelfTrans();

        Transition transition = pr.addTransition(ps);
        transition.isEventual = true;

        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, ps, p_true}, new Phase[]{pr},
                mClocks);
        pea.setDestPhase(p_true);
        Set<Phase> phase = new HashSet<>();
        Collections.addAll(phase, pea.getPhases());
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), CDD.TRUE, CDD.TRUE, CDD.TRUE, phase));
        return pea;
    }

    @Override
    void handleOnePair(CDD R, CDD S, int i, int j, HashMap<String, Phase> phases, ArrayList<Phase> init, Phase p_true) {
        String rName = id + "_st1_" + i, sName = id + "_st2_" + j;
        Phase pr = phases.get(rName);
        if (pr == null) {
            pr = new Phase(rName, R);
            pr.addSelfTrans();

            phases.put(rName,pr);
            init.add(pr);
        }
        Phase ps = phases.get(sName);
        if (ps == null) {
            ps = new Phase(sName, S);
            ps.addSelfTrans();
            ps.addTransition(p_true);

            phases.put(sName,ps);
        }
        Transition transition = pr.addTransition(ps);
        transition.isEventual = true;
    }

    @Override
    public PEAFragment gen4merge() {
        return gen4mergeCommon();
    }
}
