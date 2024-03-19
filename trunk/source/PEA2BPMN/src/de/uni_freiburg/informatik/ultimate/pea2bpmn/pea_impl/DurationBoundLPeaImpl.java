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

public class DurationBoundLPeaImpl implements IPeaImpl<DurationBoundLPattern> {
    private final DurationBoundLPattern mReq;

    public DurationBoundLPeaImpl(PatternType<?> req) {
        mReq = (DurationBoundLPattern)req;
    }

    @Override
    public PEAFragment generate() {
        final SrParseScope<?> scope = mReq.getScope();
        final CDD R = mReq.getCdds().get(0);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();

        String rClock = R + "t";
        CDD constraintDr = RangeDecision.create(rClock, RangeDecision.OP_GTEQ, c1);
        Phase pr = new Phase("st1", R, constraintDr);

        String peaName = mReq.getId() + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr}, new Phase[]{pr},
                Collections.singletonList(rClock));
        pea.addOut(pr, constraintDr);

        pea.setDesc(new ReqDesc(mReq, List.of(), List.of(R), CDD.TRUE, CDD.TRUE, constraintDr));
        return pea;
    }
}
