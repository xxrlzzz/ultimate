package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResponseBoundL1PeaImpl extends AbsPeaImpl<ResponseBoundL1Pattern> {
//    private final ResponseBoundL1Pattern mReq;
    // ResponseBoundL1Pattern or EdgeResponseBoundL1Pattern

    public ResponseBoundL1PeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(1);
        mClocks.add(R.toString() + "c");
    }

    @Override
    public PEAFragment generate() {
        // P and Q are reserved for scope.
        // R, S, ... are reserved for CDDs, but they are parsed in reverse order.
        final SrParseScope<?> scope = mReq.getScope();
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);
        final CDD R_S = R.and(S);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();

//        String rClock = R.toString() + "t";
        String rClock = mClocks.get(0);
        CDD conditionStay = RangeDecision.create(rClock, RangeDecision.OP_LTEQ, c1);
//        Phase pr = new Phase(id + "_st1", R, conditionStay);
        Phase pr = new Phase(id + "_st1", R);
        Phase ps = new Phase(id + "_st2", S);
        Phase prs = new Phase(id + "_st3", R_S);

        // 这里R的子循环边可以考虑时钟。
        pr.addSelfTrans();
        ps.addSelfTrans();
        prs.addSelfTrans();
//        Phase p_true = new Phase(id + "_st4", CDD.TRUE);
        Phase p_true = new Phase(id + "_st4", R.negate().and(S.negate()));
        ps.addSimpleTran(p_true);
        prs.addSimpleTran(p_true);
        pr.addSimpleTran(p_true);
        p_true.addSelfTrans();

        CDD conditionDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
        pr.addTransition(ps, conditionDr, new String[]{});
        pr.addTransition(prs, conditionDr, new String[]{});
        prs.addTransition(ps, conditionDr, new String[]{});
        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, ps, prs, p_true}, new Phase[]{pr},
                mClocks);
//        PEAFragment pea = new PEAFragment(peaName, new Phase[]{ pr, ps,p_true}, new Phase[]{pr},
//                mClocks);
        pea.addOut(ps, CDD.TRUE);
        pea.addOut(prs, CDD.TRUE);
        pea.addOut(pr, conditionStay);

        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), conditionDr, CDD.TRUE, CDD.TRUE));
        return pea;
    }
}
