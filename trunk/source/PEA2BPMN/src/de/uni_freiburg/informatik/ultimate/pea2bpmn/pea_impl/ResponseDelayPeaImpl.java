package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseDelayPattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Collections;
import java.util.List;

public class ResponseDelayPeaImpl implements IPeaImpl<ResponseDelayPattern> {
    // ResponseDelayPattern or EdgeResponseDelayPattern
    private final PatternType<?> mReq;

    public ResponseDelayPeaImpl(PatternType<?> req) {
        mReq = req;
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

        Phase pr = new Phase(id + "_st1", R);
        String ar = "After_" + R;
        String arClock = ar + "t";
        CDD condDr =  RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c1);
        Phase par = new Phase(id + "_"+ ar, CDD.TRUE, condDr);
        Phase ps = new Phase(id + "_st2", S);
        pr.addTransition(par, CDD.TRUE, new String[]{arClock});
        par.addTransition(ps, CDD.TRUE, new String[]{});

        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, par, ps}, new Phase[]{pr},
                Collections.singletonList(arClock));
        pea.addOut(ps, CDD.TRUE);
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), condDr, CDD.TRUE, CDD.TRUE));
        return pea;
    }
}
