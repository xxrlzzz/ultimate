package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import java.util.List;

public class MergeDesc {
    /**
     * complete
     * condition
     * constraint
     * sequence
     */
    public String type;

    public String leftId;
    public String rightId;

    public List<String> mergeTargets;
}
