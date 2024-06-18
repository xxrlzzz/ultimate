package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

public interface IPeaImpl<T extends PatternType<?>> {
    PEAFragment generate();
    PEAFragment gen4merge();
}
