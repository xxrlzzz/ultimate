package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.TriggerResponseBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

import java.util.Collections;

public class TriggerResponseBoundL1PeaImpl implements IPeaImpl<TriggerResponseBoundL1Pattern> {
    private final TriggerResponseBoundL1Pattern mReq;

    public TriggerResponseBoundL1PeaImpl(PatternType<?> req) {
        mReq = (TriggerResponseBoundL1Pattern) req;
    }


    @Override
    public PEAFragment generate() {
        final SrParseScope<?> scope = mReq.getScope();
        final String id = mReq.getId();
        final CDD R = mReq.getCdds().get(2);
        final CDD S = mReq.getCdds().get(1);
        final CDD R_S = R.and(S);
        final CDD T = mReq.getCdds().get(0);
        final CDD S_T = S.and(T);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();

        String rClock = R + "t";
        CDD rtLess = RangeDecision.create(rClock, RangeDecision.OP_LT, c1);
        CDD rtGteq = rtLess.negate();
        // TODO: add reset for pr and prs
        Phase pr = new Phase(id + "_st1", R);
        Phase prs = new Phase(id + "_st2", R_S);
        Phase pst = new Phase(id + "_st3", S_T);

        pr.addTransition(pst, rtGteq, new String[]{});
        prs.addTransition(pst, rtGteq, new String[]{});
        pr.addTransition(prs, CDD.TRUE, new String[]{});
        prs.addTransition(pr, CDD.TRUE, new String[]{});

        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, prs, pst}, new Phase[]{pr, prs},
                Collections.singletonList(rClock));
        pea.addOut(pr, rtLess.or(S.negate()));
        pea.addOut(prs, rtLess.or(S.negate()));
        pea.addOut(pst, CDD.TRUE);
        // TODO ReqDesc for trigger
        return pea;
    }
}
