package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.ResponseDelayPattern;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.*;

/**
 * ResponseDelay 的验证要调整<=为<
 * 进来的边 怎么reset？
 */
public class ResponseDelayPeaImpl extends AbsPeaImpl<ResponseDelayPattern> {
    // ResponseDelayPattern or EdgeResponseDelayPattern
    final int c1;

    public ResponseDelayPeaImpl(PatternType<?> req) {
        super(req);
        // P and Q are reserved for scope.
        // R, S, ... are reserved for CDDs, but they are parsed in reverse order.
        final SrParseScope<?> scope = mReq.getScope();

        final CDD R = mReq.getCdds().get(1);
        c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();
        String ar = "After_" + R.toUppaalString() + "c";
        mClocks.add(ar);
    }

    @Override
    public PEAFragment generate() {
        final CDD R = mReq.getCdds().get(1);
        final CDD S = mReq.getCdds().get(0);
        final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();

        String ar = "After_" + R;
//        String arClock = ar + "t";
        String arClock = mClocks.get(0);
        CDD consDl =  RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c1);
        Phase pr = new Phase(id + "_st1", R, RangeDecision.create(arClock, RangeDecision.OP_LTEQ, 1));
        Phase par = new Phase(id + "_"+ ar, R.negate(), consDl);
        Phase ps = new Phase(id + "_st2", S);

        Phase prs = new Phase(id + "_st3", R.and(S));
        Phase par2 = new Phase(id + "_" + ar + "_2", R, consDl);

        Phase p_true = new Phase(id + "_st4", R.negate().and(S.negate()));

        pr.addTransition(par);
        par.addTransition(ps);
        pr.addTransition(par2);
        par2.addTransition(ps);
        par2.addTransition(prs);
        prs.addTransition(ps);

        pr.addTransition(pr, RangeDecision.create(arClock, RangeDecision.OP_LT, 1));
        par.addSelfTrans();
        par2.addSelfTrans();
        ps.addSelfTrans();
        prs.addSelfTrans();

        ps.addTransition(p_true);
        prs.addTransition(p_true);
        p_true.addSelfTrans();

        PEAFragment pea = new PEAFragment(getPEAName(), new Phase[]{pr, par, ps,par2,prs,p_true}, new Phase[]{pr},
                mClocks);
        pea.setDestPhase(p_true);
        pea.setDesc(new ReqDesc(mReq, List.of(R), List.of(S), CDD.TRUE, consDl, CDD.TRUE));
        return pea;
    }

    @Override
    void handleOnePair(CDD R, CDD S, int i, int j, HashMap<String, Phase> phases, ArrayList<Phase> init, Phase p_true) {
        String arClock = mClocks.get(0);
        CDD consDl = RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c1);
        String ar = "After_" + R;
        CDD condDr1 = RangeDecision.create(arClock, RangeDecision.OP_LT, 1);
        CDD condDr1_eq = RangeDecision.create(arClock, RangeDecision.OP_LTEQ, 1);
        String rName = id + "_st1_" + i, arName = id + "_st1_" + ar, sName = id + "_st2_" + j;
        Phase pr = phases.get(rName);
        Phase par = phases.get(arName);
        if (pr == null) {
            pr = new Phase(rName, R, condDr1_eq);
            pr.addTransition(pr, condDr1);
            assert par == null; // 两个一起的
            par = new Phase(arName, R.operator("pc"), consDl);
            par.addPhantom(R, true);
            par.addTransition(par, consDl);
            pr.addTransition(par);

            phases.put(rName,pr);
            phases.put(arName,par);
            init.add(pr);
        }
        Phase ps = phases.get(sName);
        if (ps == null) {
//            ps = new Phase(sName, R.operator("p").and(S));
            ps = new Phase(sName, S);
            ps.addSelfTrans();
//            par.addTransition(ps, consDl);
            ps.addTransition(p_true);
            phases.put(sName, ps);
        }
        ps.setStateInvariant(ps.getStateInvariant().and(R.operator("p")));
        ps.addPhantom(R, false);
        par.addTransition(ps, consDl);
    }

    @Override
    CDD consDl() {
        String arClock = mClocks.get(0);
        return RangeDecision.create(arClock, RangeDecision.OP_LTEQ, c1);
    }

    @Override
    public PEAFragment gen4merge() {
        return gen4mergeCommon();
    }
}
