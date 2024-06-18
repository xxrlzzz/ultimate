package de.uni_freiburg.informatik.ultimate.pea2bpmn;

import com.github.jhoenicke.javacup.runtime.Symbol;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.ReqParser;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.DeclarationPattern;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import de.uni_freiburg.informatik.ultimate.pea2boogie.req2pea.IReq2Pea;
import de.uni_freiburg.informatik.ultimate.pea2boogie.req2pea.Req2Pea;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl.*;
import de.uni_freiburg.informatik.ultimate.pea2boogie.testgen.Req2CauseTrackingPeaTransformer;
import de.uni_freiburg.informatik.ultimate.test.mocks.UltimateMocks;

public class PeaTestUtil {

    /**
     * Use to supply a string (instead of file) to parser.
     *
     * @param reqFileName
     * @return
     * @throws Exception
     *
     */
    public static PatternType<?>[] genPatterns(final String testInput) throws Exception {
        final IUltimateServiceProvider services = UltimateMocks.createUltimateServiceProviderMock();

        final StringReader sr = new StringReader(testInput);
        final ReqParser parser = new ReqParser(services.getLoggingService().getLogger(PeaFragmentTestsuite.class), sr, "");
        final Symbol goal = parser.parse();
        final PatternType<?>[] patterns = (PatternType[]) goal.value;

        return patterns;
    }


    public static void writeFile(String file, String content) throws IOException {
        final FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.flush();
        writer.close();
    }


    public static PhaseEventAutomata req2PEA(String req, IUltimateServiceProvider services, ILogger logger) throws Exception {
        // 1. 转换成规约对象
        final List<PatternType<?>> parsedPatterns = List.of(genPatterns(req));
        // 2. 转换成PEA对象
        List<DeclarationPattern> init = new ArrayList<>();
        final Req2CauseTrackingPeaTransformer transformer = new Req2CauseTrackingPeaTransformer(services, logger);
        final IReq2Pea req2pea = createReq2Pea(transformer, init, parsedPatterns, services, logger);
        int peaId = 0;
        List<PhaseEventAutomata> peaList = new ArrayList<>();
        for (PatternType.ReqPeas reqPea : req2pea.getReqPeas()) {
            for (Map.Entry<CounterTrace, PhaseEventAutomata> entry : reqPea.getCounterTrace2Pea()) {
                PhaseEventAutomata pea = entry.getValue();
                if (pea == null) {
                    logger.error("empty pea ", reqPea.getPattern());
                    continue;
                }
                peaList.add(pea);
                // j2uppaalWriter.writePEA2UppaalFile("/tmp/toCheck" + peaId + ".xml", pea);
                peaId++;
            }
        }
        PhaseEventAutomata pea = peaList.get(0);
        for (int i = 1; i < peaList.size(); i++) {
            pea = pea.parallel(peaList.get(i));
//			pea.parallel(peaList.get(i));
        }
        return pea;
    }


    private static IReq2Pea createReq2Pea(final Req2CauseTrackingPeaTransformer transformer,
                                          final List<DeclarationPattern> init, final List<PatternType<?>> requirements,
                                          IUltimateServiceProvider services, ILogger logger) {
        IReq2Pea req2pea = new Req2Pea(services, logger, init, requirements);
//		req2pea = transformer.transform(req2pea, init, requirements);

        return req2pea;
    }


    public static List<PEAFragment> testPattern2Impl(String req) throws Exception {
        final PatternType<?>[] parsedPatterns = genPatterns(req);

        ArrayList<PEAFragment> peaFragments = new ArrayList<>();
        for (PatternType<?> pattern : parsedPatterns) {
            if (pattern != null) {
                peaFragments.add(new PeaImplBuilder().build(pattern).gen4merge());
            }
        }
        return peaFragments;
    }
}
