package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.RangeDecision;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.EdgeResponseBoundL2Pattern;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseBoundL12Pattern;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EdgeResponseBoundL2PeaImpl implements IPeaImpl<EdgeResponseBoundL2Pattern> {
    private final EdgeResponseBoundL2Pattern mReq;

    public EdgeResponseBoundL2PeaImpl(PatternType<?> req) {
        mReq = (EdgeResponseBoundL2Pattern) req;
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

        String sClock = S + "t";
        CDD constraintDr = RangeDecision.create(sClock, RangeDecision.OP_GTEQ, c1);
        Phase pr = new Phase(id + "_st1", R);
        Phase ps = new Phase(id + "_st2", S, constraintDr);

        pr.addTransition(ps, CDD.TRUE, new String[]{sClock});
        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, ps}, new Phase[]{pr}, Collections.singletonList(sClock));
        pea.addOut(ps, constraintDr);
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), CDD.TRUE, CDD.TRUE, constraintDr));
        return pea;
    }
}
