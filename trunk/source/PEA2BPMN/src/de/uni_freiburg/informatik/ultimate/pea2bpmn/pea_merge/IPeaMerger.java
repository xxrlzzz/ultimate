package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public abstract class IPeaMerger {

    Set<Phase> phases = new HashSet<>();
    Set<String> clocks = new HashSet<>();
    Set<Phase> inits = new HashSet<>();
    Phase mConditionPhase, oConditionPhase;
    Phase mAfterCondPhase, oAfterCondPhase;
    Phase mConsPhase, oConsPhase;
    /**
     * @param left
     * @param right
     * @return 合成后的片段，如果为空则合并失败
     */
    public abstract PEAFragment merge(PEAFragment left, PEAFragment right);

    void preMerge(PEAFragment left, PEAFragment right) {
        PairMergerUtils.mergeCommon(left, right, phases, clocks, inits);

        // find the phase in two fragment.
        Phase[] findMPhases = PairMergerUtils.findPhasesInReq(left.getDesc(), left.getPhases());
        Phase[] findOPhases = PairMergerUtils.findPhasesInReq(right.getDesc(), right.getPhases());
        mConditionPhase = findMPhases[0];
        oConditionPhase = findOPhases[0];
        mAfterCondPhase = findMPhases[1];
        oAfterCondPhase = findOPhases[1];
        mConsPhase = findMPhases[2];
        oConsPhase = findOPhases[2];
    }

    PEAFragment makeFragment(String name) {
        return new PEAFragment(name, phases.toArray(new Phase[]{}),
                inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
    }
}
