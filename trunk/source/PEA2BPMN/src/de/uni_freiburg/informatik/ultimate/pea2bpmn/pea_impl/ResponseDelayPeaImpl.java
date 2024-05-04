package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseDelayPattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * ResponseDelay 的验证要调整<=为<
 */
public class ResponseDelayPeaImpl extends AbsPeaImpl<ResponseDelayPattern> {
    // ResponseDelayPattern or EdgeResponseDelayPattern

    public ResponseDelayPeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(1);

        String ar = "After_" + R + "c";
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

        String ar = "After_" + R;
//        String arClock = ar + "t";
        String arClock = mClocks.get(0);
        Phase pr = new Phase(id + "_st1", R, RangeDecision.create(arClock, RangeDecision.OP_LT, 1));
        CDD consDl =  RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c1);
        Phase par = new Phase(id + "_"+ ar, CDD.TRUE, consDl);
        Phase ps = new Phase(id + "_st2", S);

        Phase prs = new Phase(id + "_st3", R.and(S));
        Phase par2 = new Phase(id + "_" + ar + "_2", R, consDl);

        Phase p_true = new Phase(id + "_st4", R.negate().and(S.negate()));

        pr.addSimpleTran(par);
        par.addSimpleTran(ps);
        pr.addSimpleTran(par2);
        par2.addSimpleTran(ps);
        par2.addSimpleTran(prs);

        ps.addSimpleTran(p_true);
        prs.addSimpleTran(p_true);

        pr.addSelfTrans();
        par.addSelfTrans();
        par2.addSelfTrans();
        ps.addSelfTrans();
        prs.addSelfTrans();

        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, par, ps,par2,prs,p_true}, new Phase[]{pr},
                mClocks);
        pea.addOut(ps, CDD.TRUE);
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), CDD.TRUE, consDl, CDD.TRUE));
        return pea;
    }
}
