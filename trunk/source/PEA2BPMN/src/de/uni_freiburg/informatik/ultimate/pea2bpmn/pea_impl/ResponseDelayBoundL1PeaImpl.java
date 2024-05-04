package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseDelayBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResponseDelayBoundL1PeaImpl extends AbsPeaImpl<ResponseDelayBoundL1Pattern> {
    public ResponseDelayBoundL1PeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(1);

        String ar = "After_" + R + "c";
        mClocks.add(R + "c");
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

//        String arClock = ar + "t";
//        String rClock = R + "t";
        String arClock = mClocks.get(1);
        String rClock = mClocks.get(0);
        Phase pr = new Phase(id + "_st1", R);
        Phase ps = new Phase(id + "_st2", S);
        Phase prs = new Phase(id + "_st3", R.and(S));
        Phase p_true = new Phase(id + "_st4", R.negate().and(S.negate()));
        String ar = "After_" + R;

        CDD consDl = RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c2);
        CDD condDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);

        Phase par = new Phase(id + "_st_" + ar + "1", R.and(S.negate()), consDl);
        Phase par2 = new Phase(id + "_st_" + ar + "2", R.negate().and(S.negate()), consDl);
        pr.addTransition(par, condDr, new String[]{arClock});
        pr.addTransition(par2, condDr, new String[]{arClock});
        par.addSimpleTran(ps);
        par.addSimpleTran(prs);
        par.addSimpleTran(par2);
        par2.addSimpleTran(par);
        par2.addSimpleTran(ps);
        prs.addSimpleTran(ps);

        pr.addTransition(pr, condDr.negate(), new String[]{});
        ps.addSelfTrans();
        prs.addSelfTrans();
        par.addTransition(par, consDl, new String[]{});
        par2.addTransition(par2, consDl, new String[]{});
        p_true.addSelfTrans();
        ps.addSimpleTran(p_true);
        prs.addSimpleTran(p_true);

        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, par, par2, prs, ps, p_true}, new Phase[]{pr},
                List.of(arClock, rClock));
        pea.addOut(ps, CDD.TRUE);
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), condDr, consDl, CDD.TRUE));
        return pea;
    }
}
