package de.uni_freiburg.informatik.ultimate.pea2bpmn.req;

import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge.PairMergerUtils;

import java.util.*;


public class PEAFragment extends PhaseEventAutomata {
    ReqDesc mDesc;
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
}
