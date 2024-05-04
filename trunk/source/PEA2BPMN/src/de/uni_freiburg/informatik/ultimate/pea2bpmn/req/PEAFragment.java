package de.uni_freiburg.informatik.ultimate.pea2bpmn.req;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge.PairMergerUtils;

import java.util.*;


public class PEAFragment extends PhaseEventAutomata {
    ReqDesc mDesc;
    Phase mDestPhase;
    ArrayList<ReqDesc> mMergedDesc;
    ArrayList<Phase> mOut;
    HashMap<Phase, CDD> mOutCondition;

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
        mOut = new ArrayList<Phase>();
        mOutCondition = new HashMap<Phase, CDD>();
    }


    public void addOut(Phase out, CDD condition) {
        mOut.add(out);
        // 不要 outcondition
//        mOutCondition.put(out, condition);
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
            mMergedDesc = new ArrayList<>();
        }
        mMergedDesc.add(desc);
    }

    public ArrayList<ReqDesc> getMergedDesc() {
        return mMergedDesc;
    }

    public String dumpJSON() {

        StringBuilder outArray = new StringBuilder("[");
        for (int i = 0; i < mOut.size(); i++) {
            if (i > 0) {
                outArray.append(",");
            }
            outArray.append("\"");
            outArray.append(mOut.get(i).getName());
            outArray.append("\"");
//            outArray.append(mOut.get(i).getID()).append("-").append(mOut.get(i).getName());
        }
        outArray.append("]");
        StringBuilder initArray = new StringBuilder("[");
        for (int i = 0; i < mInit.length; i++) {
            if (i > 0) {
                initArray.append(",");
            }
            initArray.append("\"");
            initArray.append(mInit[i].getName());
            initArray.append("\"");
//            initArray.append(mInit[i].getID()).append("-").append(mInit[i].getName());
        }
        initArray.append("]");
        StringBuilder clockArray = new StringBuilder("[");
        for (int i = 0; i < mClocks.size(); i++) {
            if (i > 0) {
                clockArray.append(",");
            }
            clockArray.append("\"");
            clockArray.append(mClocks.get(i));
            clockArray.append("\"");
        }
        clockArray.append("]");
        StringBuilder variablesJSON = new StringBuilder("{");
        if (mVariables != null) {
            for (String key : mVariables.keySet()) {
                variablesJSON.append(String.format("\"%s\": \"%s\",", key, mVariables.get(key)));
            }
            variablesJSON.deleteCharAt(variablesJSON.lastIndexOf(","));
        }
        variablesJSON.append("}");

        StringBuilder phaseArray = new StringBuilder("[");
        for (int i = 0; i < mPhases.length; i++) {
            Phase p = mPhases[i];
            if (i > 0) {
                phaseArray.append(",");
            }
            phaseArray.append(String.format("{\"name\": \"%s\", \"state\": \"%s\", \"clock\": \"%s\"}",
                    p.getName(), p.getStateInvariant().toUppaalString(), p.getClockInvariant().toUppaalString()));
        }
        phaseArray.append("]");

        StringBuilder transArray = new StringBuilder("[");
        for (Phase p: mPhases) {
            for (Transition transition : p.getTransitions()) {
                StringBuilder resets = new StringBuilder();
                for (String reset : transition.getResets()) {
                    resets.append(reset).append(",");
                }
                transArray.append(String.format("{\"src\": \"%s\", \"dest\": \"%s\", \"guard\": \"%s\", \"reset\": \"%s\", \"parallel\": \"%b\"}",
                        transition.getSrc().getName(), transition.getDest().getName(), transition.getGuard().toUppaalString(), resets, transition.isParallel));
            }
            transArray.append(",");
        }
        transArray.deleteCharAt(transArray.lastIndexOf(","));
        transArray.append("]");
        StringBuilder reqBuilder = new StringBuilder("[");
        if (mDesc != null && mDesc.getReq() != null) {
//            req = mDesc.getReq().toString();
            reqBuilder.append(mDesc.getReq().toString());
        }
        if (mMergedDesc != null) {
            for (ReqDesc desc : mMergedDesc) {
                reqBuilder.append(desc.getReq().toString()).append(",");
            }
            reqBuilder.deleteCharAt(reqBuilder.lastIndexOf(","));
        }
        reqBuilder.append("]");

        return String.format("{\n\"name\": \"%s\",\n\"req\": \"%s\",\n\"out\": %s,\n\"init\": %s,\n" +
                        "\"clocks\": %s,\n\"vars\": %s,\n\"phases\": %s,\n\"trans\": %s\n}",
                getName(), reqBuilder, outArray, initArray, clockArray, variablesJSON, phaseArray, transArray);
    }
}
