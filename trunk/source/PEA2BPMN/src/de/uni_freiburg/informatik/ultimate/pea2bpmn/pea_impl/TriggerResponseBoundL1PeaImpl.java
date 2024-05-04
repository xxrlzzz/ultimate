package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.TriggerResponseBoundL1Pattern;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Collections;
import java.util.List;

public class TriggerResponseBoundL1PeaImpl extends AbsPeaImpl<TriggerResponseBoundL1Pattern> {
    public TriggerResponseBoundL1PeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(2);
        mClocks.add(R + "c");
    }

    @Override
    public PEAFragment generate() {
        final SrParseScope<?> scope = mReq.getScope();
        final CDD R = mReq.getCdds().get(2);
        final CDD S = mReq.getCdds().get(1);
        final CDD T = mReq.getCdds().get(0);
        final CDD R_S = R.and(S);
        final CDD S_T = S.and(T);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();

        String rClock = mClocks.get(0);
        CDD rtLess = RangeDecision.create(rClock, RangeDecision.OP_LT, c1);
        CDD rtGteq = rtLess.negate();
        final CDD exit_cnd = rtLess.or(S.negate());

        Phase pr = new Phase(id + "_st1", R);
        Phase prs = new Phase(id + "_st2", R_S);
        Phase pst = new Phase(id + "_st3", S_T);
        Phase prst = new Phase(id + "_st4", R_S.and(T));
        Phase p_true = new Phase(id + "_st5");

        pr.addTransition(pst, rtGteq, new String[]{});
        prs.addTransition(pst, rtGteq, new String[]{});
        pr.addTransition(prst, rtGteq, new String[]{});
        prs.addTransition(prst, rtGteq, new String[]{});

        pr.addSimpleTran(prs);
        prs.addSimpleTran(pr);
        prst.addSimpleTran(pst);
        p_true.addSelfTrans();

        pr.addTransition(pr, rtLess, new String[]{});
        prs.addTransition(prs, rtLess, new String[]{});

        pst.addSimpleTran(p_true);
        prst.addSimpleTran(p_true);
        pr.addTransition(p_true, exit_cnd, new String[]{});
        prs.addTransition(p_true, exit_cnd, new String[]{});

        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr, prs, pst, prst, p_true}, new Phase[]{pr, prs},
                Collections.singletonList(rClock));
        pea.addOut(pr, exit_cnd);
        pea.addOut(prs, exit_cnd);
        pea.addOut(pst, CDD.TRUE);
        pea.setDesc(new ReqDesc(mReq, List.of(R, S), List.of(T), rtLess, CDD.TRUE, CDD.TRUE));
        return pea;
    }
}
