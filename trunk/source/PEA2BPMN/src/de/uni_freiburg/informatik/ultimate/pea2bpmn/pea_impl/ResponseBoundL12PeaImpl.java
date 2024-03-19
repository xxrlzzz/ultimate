package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseBoundL12Pattern;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.ArrayList;
import java.util.List;

public class ResponseBoundL12PeaImpl implements IPeaImpl<ResponseBoundL12Pattern> {
    private final ResponseBoundL12Pattern mReq;

    public ResponseBoundL12PeaImpl(PatternType<?> req) {
        mReq = (ResponseBoundL12Pattern) req;
    }


    @Override
    public PEAFragment generate() {
        // P and Q are reserved for scope.
        // R, S, ... are reserved for CDDs, but they are parsed in reverse order.
        final SrParseScope<?> scope = mReq.getScope();
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();
        final int c2 = SmtUtils.toInt(mReq.getDurations().get(1)).intValueExact();

        String rClock = R + "t";
        String sClock = S + "t";

        CDD conditionDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
        CDD consDr =  RangeDecision.create(sClock, RangeDecision.OP_GTEQ, c2);
        Phase pr = new Phase("st1", R);
        Phase ps = new Phase("st2", S, consDr);
        pr.addTransition(ps, conditionDr, new String[]{sClock});
        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, ps}, new Phase[]{pr}, List.of(rClock, sClock));
        pea.addOut(ps, consDr);
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), conditionDr, CDD.TRUE, consDr));
        return pea;
    }
}
