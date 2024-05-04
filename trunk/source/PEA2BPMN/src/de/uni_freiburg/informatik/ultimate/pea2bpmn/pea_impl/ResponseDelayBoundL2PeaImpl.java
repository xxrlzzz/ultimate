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

import java.util.List;

public class ResponseDelayBoundL2PeaImpl extends AbsPeaImpl<ResponseDelayBoundL2Pattern> {
    //  ResponseDelayBoundL2Pattern or EdgeResponseDelayBoundL2Patter
    public ResponseDelayBoundL2PeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);
        String ar = "After_" + R + "c";
        mClocks.add(S + "c");
        mClocks.add(ar);
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

        String ar = "After_" + R;
        String arClock = mClocks.get(1);
        String sClock = mClocks.get(0);
        CDD rDr = RangeDecision.create(arClock, RangeDecision.OP_LT, 1);
        Phase pr = new Phase(id + "_st1", CDD.TRUE);
        Phase p_true = new Phase(id + "_st3", CDD.TRUE);

        CDD consDl = RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c1);
        CDD consDr = RangeDecision.create(sClock, RangeDecision.OP_GTEQ, c2);
        Phase par = new Phase(id + "_st_" + ar, CDD.TRUE, consDl);
        Phase prs = new Phase(id + "_st2", S, CDD.TRUE);
        pr.addSimpleTran(par);
        par.addTransition(prs, consDl, new String[]{sClock});

        pr.addTransition(pr, rDr, new String[]{});
        par.addTransition(par, consDl, new String[]{});
        prs.addSelfTrans();
        p_true.addSelfTrans();
        prs.addTransition(p_true, consDr, new String[]{});

        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, par, prs}, new Phase[]{pr},
                List.of(arClock, sClock));
        pea.addOut(prs, consDr);

        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), CDD.TRUE, consDl, consDr));
        return pea;
    }
}
