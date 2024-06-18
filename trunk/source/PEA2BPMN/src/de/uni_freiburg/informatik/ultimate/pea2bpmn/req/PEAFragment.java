package de.uni_freiburg.informatik.ultimate.pea2bpmn.req;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import de.uni_freiburg.informatik.ultimate.lib.pea.*;

import java.util.*;


public class PEAFragment extends PhaseEventAutomata {
    ReqDesc mDesc;
    Phase mDestPhase;
    HashSet<ReqDesc> mMergedDesc;
    String mEntryReset;

    public PEAFragment(final String name, final Phase[] phases, final Phase[] init) {
        this(name, phases, init, new ArrayList<String>());
    }

    public PEAFragment(final String name, final Phase[] phases, final Phase[] init, final List<String> clocks) {
        this(name, phases, init, clocks, null, null);
    }

    public PEAFragment(final String name, final Phase[] phases, final Phase[] init, final List<String> clocks,
                       final Map<String, String> variables, final List<String> declarations) {
        this(name, phases, init, clocks, variables, null, declarations);
    }

    /**
     * @param clocks
     * @param declarations
     * @param init
     * @param name
     * @param phases
     * @param variables
     */
    public PEAFragment(final String name, final Phase[] phases, final Phase[] init, final List<String> clocks,
                       final Map<String, String> variables, final Set<String> events, final List<String> declarations) {
        super(name, phases, init, clocks, variables, events, declarations);
    }

    public void setEntryReset(String rst) {
        mEntryReset = rst;
    }

    public String getEntryReset() {
        return mEntryReset;
    }

    public void setDestPhase(Phase dest) {
        mDestPhase = dest;
    }

    public Phase getDestPhase() {
        return mDestPhase;
    }

    public void setDesc(ReqDesc desc) {
        mDesc = desc;
    }

    public ReqDesc getDesc() {
        return mDesc;
    }

    public void addMergedDesc(ReqDesc desc) {
        if (desc == null) {
            return;
        }
        if (mMergedDesc == null) {
            mMergedDesc = new HashSet<>();
        }
        mMergedDesc.add(desc);
    }

    public Set<ReqDesc> getMergedDesc() {
        return mMergedDesc;
    }

    /**
     * 根据约束条件找 ReqDesc
     * 不考虑 cons 有 phantom
     * @param cons
     * @return
     */
    public ReqDesc findDescByCons(CDD cons) {
        if (mDesc != null) {
            if (mDesc.constraintContain(cons)) {
                return mDesc;
            }
            if (mDesc.constraintImply(cons)) {
                return mDesc;
            }
            throw new RuntimeException("非法的 cons，与 mDesc 不符合\t" + cons +"\t" + mDesc.firstConstraint() + "\t" + mDesc.getReq());
        }
        if (mMergedDesc == null) {
            return null;
        }
        for (ReqDesc desc : mMergedDesc) {
            if (desc.constraintContain(cons)) {
                return desc;
            }
            if (desc.constraintImply(cons)) {
                return desc;
            }
        }
        return null;
    }

    /**
     * 只找After
     * @param scope
     * @return
     */
    public ReqDesc findDescByScope(CDD scope) {
        if (mDesc != null) {
            CDD reqScope = mDesc.getReq().getScope().getCdd1();
            if (reqScope.implies(scope)) {
                return mDesc;
            }
            if (scope.implies(reqScope)) {
                return mDesc;
            }
            throw new RuntimeException("非法的 scope，与 mDesc 不符合\t" + scope + "\t" + mDesc.getReq());
        }
        if (mMergedDesc == null) {
            return null;
        }
        for (ReqDesc desc : mMergedDesc) {
            if (desc.getReq().getScope().getCdd1() != null) {
                CDD reqScope = desc.getReq().getScope().getCdd1();
                if (reqScope.implies(scope)) {
                    return desc;
                }
                if (scope.implies(reqScope)) {
                    return desc;
                }
                return desc;
            }

        }
        return null;
    }

    public ReqDesc findDescByCond(CDD cond) {
        if (mDesc != null) {
//            System.out.println(mDesc + "\t" + mDesc.firstCondition() + "\t" + cond);
            if (mDesc.conditionContain(cond)) {
                return mDesc;
            }
            throw new RuntimeException("非法的 cond cons，与 mDesc 不符合\t" + cond + "\t" + mDesc.getReq());
        }
        if (mMergedDesc == null) {
            return null;
        }
        for (ReqDesc desc : mMergedDesc) {
            if (desc.conditionContain(cond)) {
                return desc;
            }
        }
        return null;
    }


    public ReqDesc findDescByCondAndCons(CDD cond, CDD cons) {
        assert cond != null && cons != null;
        if (mDesc != null) {
            if (mDesc.conditionContain(cond) && mDesc.constraintContain(cons)) {
                return mDesc;
            }
            throw new RuntimeException("非法的 cond cons，与 mDesc 不符合\t" + cond + "\t" + cons + "\t" + mDesc.getReq());
        }
        if (mMergedDesc == null) {
            return null;
        }
        for (ReqDesc desc : mMergedDesc) {
            if (desc.conditionContain(cond) && desc.constraintContain(cons)) {
                return desc;
            }
        }
        return null;
    }


    /**
     * 去掉phantom，一次去一个
     * @return
     */
    public PEAFragment dePhantom() {
        List<Phase> nPhases = new ArrayList<>();
        boolean removed = false;
        for (Phase mPhase : mPhases) {
            if (mPhase.hasPhantom() && !removed) {
                // 对于每个有 phantom 算子的状态
                // 先让状态去phantom，形成列表。
                List<Phase> phases = mPhase.dePhantom();
                // 找到其他有向此状态变迁的状态，把变迁添加到拆分后状态中。
                for (Phase oPhase : mPhases) {
                    Transition tr = oPhase.getOutgoingTransition(mPhase);
                    if (oPhase != mPhase && tr != null) {
                        phases.forEach(p -> oPhase.addTransition(p, tr.getGuard(), tr.getResets()));
                    }
                    oPhase.removeTransition(tr);
                }
                // 更新 phases 列表
                nPhases.addAll(phases);
                removed = true;
            } else {
                nPhases.add(mPhase);
            }
        }
        // 如果 初始节点都被phantom了，不考虑
        if (removed) {
            return new PEAFragment(this.getName() + "_deP", nPhases.toArray(new Phase[]{}), this.getInit(),
                    this.getClocks(), this.getVariables(), this.getDeclarations()).dePhantom();
        } else {
            return this;
        }
//        return new PEAFragment(this.getName() + "_deP", nPhases.toArray(new Phase[]{}), this.getInit(),
//                this.getClocks(), this.getVariables(), this.getDeclarations());
    }

    public String dumpJSON() {
        JSONObject result = new JSONObject();
        JSONArray initArray = new JSONArray();
        for (Phase value : mInit) {
            initArray.add(value.getName());
        }
        JSONArray clockArray = JSONArray.from(mClocks);
        JSONObject variables = new JSONObject();
        if (mVariables != null) {
            variables = new JSONObject(mVariables);
        }
        JSONArray phase = new JSONArray();
        for (Phase p : mPhases) {
            JSONObject obj = new JSONObject();
            obj.put("name", p.getName());
            obj.put("state", p.getStateInvariant().toUppaalString());
            obj.put("clock", p.getClockInvariant().toUppaalString());
            phase.add(obj);
        }
        JSONArray trans = new JSONArray();
        for (Phase p : mPhases) {
            for (Transition transition : p.getTransitions()) {
                JSONObject obj = new JSONObject();
                obj.put("src", transition.getSrc().getName());
                obj.put("dest", transition.getDest().getName());
                obj.put("guard", transition.getGuard().toUppaalString());
                obj.put("reset", transition.getResets());
                obj.put("clock_writer", transition.getClockWriter());
                obj.put("parallel", transition.isParallel);
                obj.put("eventually", transition.isEventual);
                obj.put("target", transition.mTarget.toUppaalString());
                trans.add(obj);
            }
        }
        JSONArray req = new JSONArray();
        if (mDesc != null && mDesc.getReq() != null) {
            req.add(mDesc.getReq().toString());
        }
        if (mMergedDesc != null) {
            for (ReqDesc desc : mMergedDesc) {
                req.add(desc.getReq().toString());
            }
        }
        result.put("name", getName());
        result.put("req", req);
        result.put("init", initArray);
        result.put("clocks", clockArray);
        result.put("vars", variables);
        result.put("phases", phase);
        result.put("trans", trans);
        return result.toJSONString();
    }
}
