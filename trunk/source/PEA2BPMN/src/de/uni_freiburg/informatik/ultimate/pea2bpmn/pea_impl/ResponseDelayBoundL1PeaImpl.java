package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseDelayBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Collections;
import java.util.List;

public class ResponseDelayBoundL1PeaImpl implements IPeaImpl<ResponseDelayBoundL1Pattern> {
    private final ResponseDelayBoundL1Pattern mReq;

    public ResponseDelayBoundL1PeaImpl(PatternType<?> req) {
        mReq = (ResponseDelayBoundL1Pattern)req;
    }

    @Override
    public PEAFragment generate() {
        // P and Q are reserved for scope.
        // R, S, ... are reserved for CDDs, but they are parsed in reverse order.
        final SrParseScope<?> scope = mReq.getScope();
        final String id = mReq.getId();
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();
        final int c2 = SmtUtils.toInt(mReq.getDurations().get(1)).intValueExact();

        Phase pr = new Phase(id + "_st1", R);
        Phase ps = new Phase(id + "st_2", S);
        String ar = "After_" + R;
        String arClock = ar + "t";
        String rClock = R + "t";

        CDD consDl = RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c2);
        CDD condDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
        Phase par = new Phase(id + "_" +ar, CDD.TRUE, consDl);
        pr.addTransition(par, condDr, new String[]{arClock});
        par.addTransition(ps, CDD.TRUE, new String[]{});

        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, par, ps}, new Phase[]{pr},
                List.of(arClock, rClock));
        pea.addOut(ps, CDD.TRUE);
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), condDr, consDl, CDD.TRUE));
        return pea;
    }
}
