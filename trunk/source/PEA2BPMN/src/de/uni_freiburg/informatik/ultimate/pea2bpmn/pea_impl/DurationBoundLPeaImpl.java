package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.DurationBoundLPattern;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.Collections;
import java.util.List;

public class DurationBoundLPeaImpl extends AbsPeaImpl<DurationBoundLPattern> {
    public DurationBoundLPeaImpl(PatternType<?> req) {
        super(req);
        final CDD R = mReq.getCdds().get(0);
        mClocks.add(R + "c");
    }

    @Override
    public PEAFragment generate() {
        final SrParseScope<?> scope = mReq.getScope();
        final CDD R = mReq.getCdds().get(0);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();

        String rClock = mClocks.get(0);
        CDD consDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
        Phase pr = new Phase(id + "_st1", R, consDr);
        Phase p_true = new Phase(id + "_st2", CDD.TRUE);
        pr.addSelfTrans();
        pr.addTransition(p_true, consDr, new String[]{});
        p_true.addSelfTrans();
        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr}, new Phase[]{pr},
                Collections.singletonList(rClock));
        pea.addOut(pr, consDr);

        pea.setDesc(new ReqDesc(mReq, List.of(), List.of(R), CDD.TRUE, CDD.TRUE, consDr));
        return pea;
    }
}
