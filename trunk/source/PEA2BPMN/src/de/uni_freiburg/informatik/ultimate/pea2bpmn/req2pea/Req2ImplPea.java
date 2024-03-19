package de.uni_freiburg.informatik.ultimate.pea2bpmn.req2pea;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.srparse.Durations;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.DeclarationPattern;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.IReqSymbolTable;

import java.util.List;

public class Req2ImplPea implements IReq2Pea {
    private final ILogger mLogger;
    private final IUltimateServiceProvider mServices;
    public Req2ImplPea(final IUltimateServiceProvider services, final ILogger logger,
                       final List<DeclarationPattern> init, final List<PatternType<?>> reqs) {
        mLogger = logger;
        mServices = services;

        for (PatternType<?> req : reqs) {

        }
    }
    @Override
    public List<PatternType.ReqPeas> getReqPeas() {
        return null;
    }

    @Override
    public IReqSymbolTable getSymboltable() {
        return null;
    }

    @Override
    public Durations getDurations() {
        return null;
    }

    @Override
    public void transform(IReq2Pea req2pea) {

    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public IReq2PeaAnnotator getAnnotator() {
        return null;
    }
}
