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

public class ResponseBoundL12PeaImpl extends AbsPeaImpl<ResponseBoundL12Pattern> {
    public ResponseBoundL12PeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);
        mClocks.add(R.toString() + "c");
        mClocks.add(S.toString() + "c");
    }

    @Override
    public PEAFragment generate() {
        // P and Q are reserved for scope.
        // R, S, ... are reserved for CDDs, but they are parsed in reverse order.
        final SrParseScope<?> scope = mReq.getScope();
        final String id = mReq.getId();
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);
        final CDD R_S = R.and(S);
        String rClock = mClocks.get(0);
        String sClock = mClocks.get(1);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();
        final int c2 = SmtUtils.toInt(mReq.getDurations().get(1)).intValueExact();

//        String rClock = R + "t";
//        String sClock = S + "t";

        CDD conditionDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
        CDD consDr =  RangeDecision.create(sClock, RangeDecision.OP_GTEQ, c2);
        Phase pr = new Phase(id + "_st1", R);
        Phase ps = new Phase(id + "_st2", S);
        Phase prs = new Phase(id + "_st3", R_S);
        Phase p_true = new Phase(id + "_st4", R.negate().and(S.negate()));
        pr.addSelfTrans();
        ps.addSelfTrans();
        prs.addSelfTrans();
        p_true.addSelfTrans();

        pr.addTransition(ps, conditionDr, new String[]{sClock});
        pr.addTransition(prs, conditionDr, new String[]{sClock});
        prs.addSimpleTran(ps);
        pr.addTransition(p_true, conditionDr.negate(), new String[]{});
        prs.addSimpleTran(p_true);
        ps.addSimpleTran(p_true);
        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, ps,prs,p_true}, new Phase[]{pr}, mClocks);
        pea.addOut(ps, consDr);
        pea.addOut(prs, consDr);
        pea.addOut(pr, conditionDr.negate());
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), conditionDr, CDD.TRUE, consDr));
        return pea;
    }
}
