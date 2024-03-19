package de.uni_freiburg.informatik.ultimate.pea2bpmn.results;

import java.util.Set;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.pea2bpmn.Activator;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.generator.RtInconcistencyConditionGenerator;
import de.uni_freiburg.informatik.ultimate.core.lib.results.GenericResult;

public final class RequirementInconsistentErrorResult extends GenericResult {

	private final Set<String> mIds;

	public RequirementInconsistentErrorResult(final RtInconcistencyConditionGenerator.InvariantInfeasibleException ex) {
		super(Activator.PLUGIN_ID, "Requirements set is inconsistent.",
				"Requirements set is inconsistent. " + ex.getMessage(), Severity.ERROR);
		mIds = ex.getResponsibleRequirements().stream().map(a -> a.getId()).collect(Collectors.toSet());
	}

	public Set<String> getIds() {
		return mIds;
	}
}