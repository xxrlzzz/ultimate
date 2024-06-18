package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.TriggerResponseDelayBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.*;

public class TriggerResponseDelayBoundL1PeaImpl extends AbsPeaImpl<TriggerResponseDelayBoundL1Pattern> {
    public TriggerResponseDelayBoundL1PeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(2);
        final CDD S = mReq.getCdds().get(1);
        String af_rs = "After_" + R + "_" + S;
        mClocks.add(R + "c");
        mClocks.add(af_rs + "c");
    }


    @Override
    public PEAFragment generate() {
        final SrParseScope<?> scope = mReq.getScope();
        final String id = mReq.getId();
        final CDD R = mReq.getCdds().get(2);
        final CDD S = mReq.getCdds().get(1);
        final CDD T = mReq.getCdds().get(0);
        final CDD R_S = R.and(S);
        final CDD S_T = S.and(T);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();
        final int c2 = SmtUtils.toInt(mReq.getDurations().get(1)).intValueExact();

        final String af = "After_";
        String rClock = mClocks.get(0);
        String af_rsClock = mClocks.get(1);
        CDD rtLess = RangeDecision.create(rClock, RangeDecision.OP_LT, c1);
        CDD rtGteq = rtLess.negate();
        CDD consDl = RangeDecision.create(af_rsClock, RangeDecision.OP_LT, c2);
        // TODO: add reset for pr and prs
        Phase pr = new Phase(id + "_st1", R);
        Phase prs = new Phase(id + "_st2", R_S);
        Phase paf_rs = new Phase(id + "_st_" + af + R + "_" + S);
        Phase paf_r = new Phase(id + "_st_" + af + R);
        Phase paf_s = new Phase(id + "_st_" + af + R);
        Phase pt = new Phase(id + "_st3", T);
        Phase p_true = new Phase(id + "_st4");

        pr.addTransition(prs, rtLess, new String[]{});
        pr.addTransition(pr, rtLess, new String[]{});
        prs.addTransition(pr, rtLess, new String[]{});
        prs.addTransition(prs, rtLess, new String[]{});

        pr.addTransition(paf_s, rtGteq, new String[]{af_rsClock});
        prs.addTransition(paf_s, rtGteq, new String[]{af_rsClock});
        pr.addTransition(paf_rs, rtGteq, new String[]{af_rsClock});
        prs.addTransition(paf_rs, rtGteq, new String[]{af_rsClock});

        paf_r.addTransition(paf_r, consDl, new String[]{});
        paf_s.addTransition(paf_s, consDl, new String[]{});
        paf_rs.addTransition(paf_rs, consDl, new String[]{});
        paf_r.addTransition(paf_s, consDl, new String[]{});
        paf_r.addTransition(paf_rs, consDl, new String[]{});
        paf_s.addTransition(paf_r, consDl, new String[]{});
        paf_s.addTransition(paf_rs, consDl, new String[]{});
        paf_rs.addTransition(paf_s, consDl, new String[]{});
        paf_rs.addTransition(paf_r, consDl, new String[]{});

        paf_rs.addTransition(pt, consDl, new String[]{});
        paf_r.addTransition(pt, consDl, new String[]{});
        paf_s.addTransition(pt, consDl, new String[]{});
        pt.addSelfTrans();

        prs.addTransition(p_true, rtLess, new String[]{});
        pr.addTransition(p_true, rtLess, new String[]{});
        p_true.addSelfTrans();

        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, prs, paf_rs, paf_r, paf_s, pt, p_true},
                new Phase[]{pr, prs}, List.of(rClock, af_rsClock));
        pea.setDestPhase(p_true);
        Set<Phase> phase = new HashSet<>();
        Collections.addAll(phase, pea.getPhases());
        pea.setDesc(new ReqDesc(mReq, List.of(R, S), List.of(T), rtLess, consDl, CDD.TRUE, phase));
        return pea;
    }
}
