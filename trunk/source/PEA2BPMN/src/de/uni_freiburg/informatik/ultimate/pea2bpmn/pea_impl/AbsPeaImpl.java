package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;

import java.util.ArrayList;
import java.util.List;

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
}
