package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl;

import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.*;

import java.util.HashMap;
import java.util.Map;

public class PeaImplBuilder {
    private static final Map<Class<? extends PatternType<?>>, Class<? extends IPeaImpl<?>>> CONSTRUCTORS = new HashMap<>();

    static {
        CONSTRUCTORS.put(ResponsePattern.class, ResponsePeaImpl.class);

        CONSTRUCTORS.put(DurationBoundLPattern.class, DurationBoundLPeaImpl.class);
        CONSTRUCTORS.put(DurationBoundUPattern.class, DurationBoundUPeaImpl.class);
        CONSTRUCTORS.put(ResponseBoundL1Pattern.class, ResponseBoundL1PeaImpl.class);
        CONSTRUCTORS.put(ResponseBoundL12Pattern.class, ResponseBoundL12PeaImpl.class);
        CONSTRUCTORS.put(ResponseDelayBoundL1Pattern.class, ResponseDelayBoundL1PeaImpl.class);
        CONSTRUCTORS.put(ResponseDelayBoundL2Pattern.class, ResponseDelayBoundL2PeaImpl.class);
        CONSTRUCTORS.put(ResponseDelayPattern.class, ResponseDelayPeaImpl.class);
        CONSTRUCTORS.put(TriggerResponseBoundL1Pattern.class, TriggerResponseBoundL1PeaImpl.class);
        CONSTRUCTORS.put(TriggerResponseDelayBoundL1Pattern.class, TriggerResponseDelayBoundL1PeaImpl.class);

        CONSTRUCTORS.put(EdgeResponseDelayPattern.class, ResponseDelayPeaImpl.class);
        CONSTRUCTORS.put(EdgeResponseDelayBoundL2Pattern.class, ResponseDelayBoundL2PeaImpl.class);
        CONSTRUCTORS.put(EdgeResponseBoundL2Pattern.class, EdgeResponseBoundL2PeaImpl.class);

        CONSTRUCTORS.put(UniversalityPattern.class, InitializationPeaImpl.class);
        CONSTRUCTORS.put(InitializationPattern.class, InitializationPeaImpl.class);
        CONSTRUCTORS.put(UniversalityDelayPattern.class, UniversityDelayPeaImpl.class);
    }

    public IPeaImpl<?> build(PatternType<?> pattern) {
        Class<? extends IPeaImpl<?>> clazz = CONSTRUCTORS.get(pattern.getClass());
        if (clazz == null) {
            throw new IllegalArgumentException("not impl yet for pattern " + pattern.getClass());
        }
        try {
            System.out.println("generate for " + pattern.getClass());
            return clazz.getConstructor(PatternType.class).newInstance(pattern);
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
