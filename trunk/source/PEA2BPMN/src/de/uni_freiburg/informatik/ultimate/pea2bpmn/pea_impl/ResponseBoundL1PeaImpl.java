package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Collections;
import java.util.List;

public class ResponseBoundL1PeaImpl implements IPeaImpl<ResponseBoundL1Pattern> {
//    private final ResponseBoundL1Pattern mReq;
    // ResponseBoundL1Pattern or EdgeResponseBoundL1Pattern
    private final PatternType<?> mReq;

    public ResponseBoundL1PeaImpl(PatternType<?> req) {
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
        Phase ps = new Phase(id + "_st2", S);
        String rClock = R.toString() + "t";

        CDD conditionDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
        pr.addTransition(ps, conditionDr, new String[]{});
        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, ps}, new Phase[]{pr},
                Collections.singletonList(rClock));
        pea.addOut(ps, CDD.TRUE);

        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), conditionDr, CDD.TRUE, CDD.TRUE));
        return pea;
    }
}
