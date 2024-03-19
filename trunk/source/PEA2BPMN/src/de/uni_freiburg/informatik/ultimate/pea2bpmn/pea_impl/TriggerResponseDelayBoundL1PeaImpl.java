package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.TriggerResponseDelayBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

import java.util.ArrayList;

public class TriggerResponseDelayBoundL1PeaImpl implements IPeaImpl<TriggerResponseDelayBoundL1Pattern>{
    private final TriggerResponseDelayBoundL1Pattern mReq;

    public TriggerResponseDelayBoundL1PeaImpl(PatternType<?> req) {
        mReq = (TriggerResponseDelayBoundL1Pattern)req;
    }


    @Override
    public PEAFragment generate() {
        final SrParseScope<?> scope = mReq.getScope();
        final CDD R = mReq.getCdds().get(2);
        final CDD S = mReq.getCdds().get(1);
        final CDD R_S = R.and(S);
        final CDD T = mReq.getCdds().get(0);
//        final CDD S_T = S.and(T);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();
        final int c2 = SmtUtils.toInt(mReq.getDurations().get(1)).intValueExact();

        String rClock = R + "t";
        String af_rs = "After_R&S";
        String af_rsClock = af_rs + "t";
        CDD rtLess = RangeDecision.create(rClock, RangeDecision.OP_LT, c1);
        CDD rtGteq = rtLess.negate();
        // TODO: add reset for pr and prs
        Phase pr = new Phase("st1", R);
        Phase prs = new Phase("st2", R_S);
        Phase paf_rs = new Phase(af_rs, RangeDecision.create(af_rsClock, RangeDecision.OP_LTEQ, c2));
        Phase pt = new Phase("st3", T);

        pr.addTransition(paf_rs, rtGteq, new String[]{});
        prs.addTransition(paf_rs, rtGteq, new String[]{});
        paf_rs.addTransition(pt, CDD.TRUE, new String[]{});
        pr.addTransition(prs, CDD.TRUE, new String[]{});
        prs.addTransition(pr, CDD.TRUE, new String[]{});

        ArrayList<String> clocks = new ArrayList<>();
        clocks.add(rClock);
        clocks.add(af_rsClock);
        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, prs, paf_rs, pt}, new Phase[]{pr, prs},
                clocks);
        pea.addOut(pr, rtLess.or(S.negate()));
        pea.addOut(prs, rtLess.or(S.negate()));
        pea.addOut(pt, CDD.TRUE);
        return pea;
    }
}
