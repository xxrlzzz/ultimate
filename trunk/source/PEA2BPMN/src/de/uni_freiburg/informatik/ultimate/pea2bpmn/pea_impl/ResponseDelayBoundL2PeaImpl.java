package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.RangeDecision;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseDelayBoundL2Pattern;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Collections;
import java.util.List;

public class ResponseDelayBoundL2PeaImpl implements IPeaImpl<ResponseDelayBoundL2Pattern> {
    //  ResponseDelayBoundL2Pattern or EdgeResponseDelayBoundL2Patter
    private final PatternType<?> mReq;

    public ResponseDelayBoundL2PeaImpl(PatternType<?> req) {
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
        final int c2 = SmtUtils.toInt(mReq.getDurations().get(1)).intValueExact();

        Phase pr = new Phase(id + "_st1", R);
        String ar = "After_" + R;
        String arClock = ar + "t";
        String sClock = S + "t";
        CDD constraintDl = RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c1);
        CDD constraintDr = RangeDecision.create(sClock, RangeDecision.OP_GTEQ, c2);
        Phase par = new Phase(ar, CDD.TRUE, constraintDl);
        Phase ps = new Phase(id + "_st2", S, constraintDr);

        pr.addTransition(par, CDD.TRUE, new String[]{arClock});
        par.addTransition(ps, CDD.TRUE, new String[]{});

        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, par, ps}, new Phase[]{pr},
                List.of(arClock, sClock));
        pea.addOut(ps, constraintDr);

        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), CDD.TRUE, constraintDl, constraintDr));
        return pea;
    }
}
