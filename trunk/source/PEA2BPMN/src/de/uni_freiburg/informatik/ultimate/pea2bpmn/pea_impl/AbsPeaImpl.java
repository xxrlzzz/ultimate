package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.*;

public abstract class AbsPeaImpl<T extends PatternType<?>> implements IPeaImpl<T> {
    protected final PatternType<?> mReq;
    protected final List<String> mClocks = new ArrayList<>();
    protected String id;

    public AbsPeaImpl(PatternType<?> req) {
        mReq = req;
        id = mReq.getId();
    }

    public void setClock(List<String> clocks) {
        mClocks.clear();
        mClocks.addAll(clocks);
    }

    @Override
    public PEAFragment gen4merge() {
        return generate();
    }

    public String getPEAName() {
        return id + "-" + mReq.getName();
    }

    void handleOnePair(CDD R, CDD S, int i, int j, HashMap<String, Phase> phases, ArrayList<Phase> init, Phase p_true) {
    }

    CDD condDr() {
        return CDD.TRUE;
    }
    CDD consDl() {
        return CDD.TRUE;
    }
    CDD consDr() {
        return CDD.TRUE;
    }

    /**
     * 多变量兼容版本
     * @return
     */
    public PEAFragment gen4mergeCommon() {
        final CDD Rs = mReq.getCdds().get(1);
        final CDD Ss = mReq.getCdds().get(0);
        Phase p_true = new Phase(id + "_st3", Rs.negate().and(Ss.negate()));
        // cnf 且连接  dnf 或连接
        // 条件或可以拆，且不能拆
        // 结果且或都能拆
        CDD[] Rs_dnf = Rs.toDNF(), Ss_dnf = Ss.toDNF();
        CDD[] Ss_cnf = Ss.toCNF();
        // && 与 || 都统一处理
//        if (Rs_dnf.length != 1) {
//            throw new RuntimeException("not support condition" + Rs);
//        }
        if (Ss_cnf.length != 1 && Ss_dnf.length != 1) {
            throw new RuntimeException("not support constraint" + Ss);
        }
        if (Ss_cnf.length != 1) {
            Ss_dnf = Ss_cnf;
        }

        HashMap<String, Phase> phases = new HashMap<>();
        ArrayList<Phase> init = new ArrayList<>();

        for (int i = 0; i < Rs_dnf.length; i++) {
            CDD R = Rs_dnf[i];
            for (int j = 0; j < Ss_dnf.length; j++) {
                CDD S = Ss_dnf[j];
                handleOnePair(R, S, i, j, phases, init, p_true);
            }
        }

        p_true.addSelfTrans();
        phases.put("true", p_true);

        PEAFragment pea = new PEAFragment(getPEAName(), phases.values().toArray(new Phase[0]), init.toArray(new Phase[0]),
                mClocks);
        pea.setDestPhase(p_true);
        Set<Phase> phase = new HashSet<>();
        Collections.addAll(phase, pea.getPhases());
        pea.setDesc(new ReqDesc(mReq, Arrays.asList(Rs_dnf), Arrays.asList(Ss_dnf), condDr(), consDl(), consDr(), phase));
        return pea;
    }
}
