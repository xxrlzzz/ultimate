package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.InitializationPattern;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.List;

public class InitializationPeaImpl extends AbsPeaImpl<InitializationPattern> {
    public InitializationPeaImpl(PatternType<?> req) {
        super(req);
    }

    @Override
    public PEAFragment generate() {
        final CDD R = mReq.getCdds().get(0);
        Phase pr = new Phase(id + "_st1", R);
        Phase p_true = new Phase(id + "_st2", CDD.TRUE);

        pr.addSelfTrans();
        pr.addTransition(p_true);
        p_true.addSelfTrans();
        String peaName = id + "-" + mReq.getName();
        PEAFragment pea = new PEAFragment(peaName, new Phase[]{pr}, new Phase[]{pr},
                List.of());
        pea.setDestPhase(p_true);

        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(R), CDD.TRUE, CDD.TRUE, CDD.TRUE));
        return pea;
    }

    @Override
    public PEAFragment gen4merge() {
        return generate();
    }
}
