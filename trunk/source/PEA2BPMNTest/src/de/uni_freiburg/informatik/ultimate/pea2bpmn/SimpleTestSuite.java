package de.uni_freiburg.informatik.ultimate.pea2bpmn;

import java.util.*;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.pea.modelchecking.DotWriterNew;
import de.uni_freiburg.informatik.ultimate.lib.pea.modelchecking.J2UPPAALWriterV4;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl.*;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge.*;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import de.uni_freiburg.informatik.ultimate.test.mocks.UltimateMocks;

import static de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType.*;
import static de.uni_freiburg.informatik.ultimate.pea2bpmn.PeaTestUtil.*;
import de.uni_freiburg.informatik.ultimate.lib.pea.CounterTrace.BoundTypes;


@RunWith(Parameterized.class)
public class SimpleTestSuite {

    private IUltimateServiceProvider mServices = UltimateMocks.createUltimateServiceProviderMock();
    private ILogger mLogger;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{"S"}});
    }

    public SimpleTestSuite(final String s) {
        mLogger = mServices.getLoggingService().getLogger(SimpleTestSuite.class);
    }

    @Test
    public void testSingleImpl() throws Exception {
        final String reqString =
                "REQ1_0: Globally, it is always the case that if \"R\" holds, then \"S\" holds after at most \"5\" time units."
                ;

        final List<PatternType<?>> parsedPatterns = List.of(genPatterns(reqString));
        assert parsedPatterns.size() == 1;
        ResponseDelayPeaImpl gen = new ResponseDelayPeaImpl(parsedPatterns.get(0));
        PhaseEventAutomata impl = gen.generate();
        PhaseEventAutomata impl2 = gen.gen4merge();

        String dotGen = DotWriterNew.createDotString(impl);
        String dotGen2 = DotWriterNew.createDotString(impl2);
        writeFile("/tmp/toCheckSinglePatternImpl.dot", dotGen);
        writeFile("/tmp/toCheckSinglePatternImpl2.dot", dotGen2);
        PhaseEventAutomata impl3 = ((PEAFragment)impl2).dePhantom();
        String dotGen3 = DotWriterNew.createDotString(impl3);
        writeFile("/tmp/toCheckSinglePatternImpl3.dot", dotGen3);
    }


//    @Test
    public void testSpec2Uppaal() throws Exception {
        final String reqString =
                "REQ1_0: Globally, it is always the case that if \"R\" holds for at least \"5\" time units, then \"S\" holds after at most \"10\" time units."
                ;
//				"Req1-1: Globally, it is always the case that initially \"!choose_year_month\" holds\n" +
//				"Req1-2: Globally, it is always the case that if \"choose_year_month\" holds, then \"order_cash_report\" holds after at most \"10\" time units\n" +
//				"Req1-3: After \"order_cash_report\", it is always the case that \"filter_cash_transactions\" holds after at most \"10\" time units\n"

//				"Req1-4: Globally, it is always the case that if \"filter_cash_transactions\" holds, then \"!filter_cash_transactions \" holds after at most \"10\" time units\n" +
//				"Req1-5: Globally, it is always the case that if \"filter_cash_transactions\" holds then \"query_not_empty || query_empty \" eventually holds\n" +
//				"Req1-6: Globally, it is always the case that if \"print_cash_report\" holds then \"filter_cash_transactions\" previously held and was preceded by \"query_not_empty\"\n" +
//				"Req1-7: Globally, it is always the case that if \"no_cash_out_event\" holds then \"filter_cash_transactions\" previously held and was preceded by \"query_empty\""
        ;

        final List<PatternType<?>> parsedPatterns = List.of(genPatterns(reqString));
        assert parsedPatterns.size() == 1;
        PhaseEventAutomata test = req2PEA(reqString, mServices, mLogger);
        // 1. 转换成规约对象
        ResponseDelayBoundL1PeaImpl gen = new ResponseDelayBoundL1PeaImpl(parsedPatterns.get(0));
        gen.setClock(test.getClocks());
        PhaseEventAutomata impl = gen.generate();
        PhaseEventAutomata pea = test.parallel(impl);
//		System.out.println("clock==========");
//		for (String clock : test.getClocks()) {
//			System.out.println(clock);
//		}

        String dotTest = DotWriterNew.createDotString(test);
        String dotGen = DotWriterNew.createDotString(impl);
        String dotAll = DotWriterNew.createDotString(pea);
        writeFile("/tmp/toCheckSinglePatternTest.dot", dotTest);
        writeFile("/tmp/toCheckSinglePatternImpl.dot", dotGen);
        writeFile("/tmp/toCheckSinglePatternAll.dot", dotAll);
        final J2UPPAALWriterV4 j2uppaalWriter = new J2UPPAALWriterV4();
        j2uppaalWriter.writePEA2UppaalFile("/tmp/toCheck" + "SinglePatternReqTest" + ".xml", test);
        j2uppaalWriter.writePEA2UppaalFile("/tmp/toCheck" + "SinglePatternAllTest" + ".xml", pea);
    }


//    @Test
    public void testConditionMerge() throws Exception {
        final String testString =
                "REQ1_0: Globally, it is always the case that if \"R\" holds for at least \"5\" time units, then \"S\" holds afterwards.\n" +
                "REQ1_1: Globally, it is always the case that if \"R\" holds for at least \"3\" time units, then \"T\" holds afterwards for at least \"10\" time units\n."
                ;
        List<PEAFragment> peaFragments = testPattern2Impl(testString);

//        writeFile("/tmp/toCheckConditionMergeTest-input1.dot", DotWriterNew.createDotString(peaFragments.get(0)));
//        writeFile("/tmp/toCheckConditionMergeTest-input2.dot", DotWriterNew.createDotString(peaFragments.get(1)));
        PEAFragment merged = new ConditionMerge().merge(peaFragments.get(0), peaFragments.get(1));
        String dot = DotWriterNew.createDotString(merged);
        writeFile("/tmp/toCheckConditionMergeTest.dot", dot);
    }

//    @Test
    public void testConstraintMerge() throws Exception {
        final String testString =
                "REQ1_0: Globally, it is always the case that if \"R\" holds for at least \"5\" time units, then \"T\" holds afterwards.\n" +
                "REQ1_1: Globally, it is always the case that if \"S\" holds for at least \"5\" time units, then \"T\" holds afterwards for at least \"10\" time units\n."
                ;

        List<PEAFragment> peaFragments = testPattern2Impl(testString);
        PEAFragment merged = new ConstraintMerge().merge(peaFragments.get(0), peaFragments.get(1));
        String dot = DotWriterNew.createDotString(merged);
        writeFile("/tmp/toCheckConstraintMergeTest.dot", dot);
    }

//    @Test
    public void testSequenceMerge() throws Exception {
        final String testString =
                "REQ1_0: Globally, it is always the case that if \"R\" holds for at least \"5\" time units, then \"S\" holds afterwards.\n" +
                "REQ1_1: Globally, it is always the case that if \"S\" holds for at least \"5\" time units, then \"T\" holds afterwards for at least \"10\" time units\n."
                ;
        List<PEAFragment> peaFragments = testPattern2Impl(testString);
		PEAFragment merged = new SequenceMerge().merge(peaFragments.get(0), peaFragments.get(1));
//        PEAFragment merged = new PartSequenceMerge().merge(peaFragments.get(0), peaFragments.get(1));
        String dot = DotWriterNew.createDotString(merged);
        writeFile("/tmp/toCheckSequenceMergeTest.dot", dot);
    }

//    @Test
    public void testPartSequenceMerge() throws Exception {
        final String testString =
                "REQ1_0: Globally, it is always the case that if \"R\" holds for at least \"5\" time units, then \"S\" holds afterwards.\n" +
                "REQ1_1: Globally, it is always the case that if \"S && F\" holds for at least \"5\" time units, then \"T\" holds afterwards for at least \"10\" time units\n."
                ;
        List<CDD> mergeTargets = new ArrayList<CDD>();
        mergeTargets.add(BooleanDecision.create("S"));
        List<PEAFragment> peaFragments = testPattern2Impl(testString);
//		for (Phase phase : peaFragments.get(1).getPhases()) {
//			System.out.println(phase.getStateInvariant());
//		}
//		System.out.println(Arrays.toString());
        PEAFragment merged = new PartSequenceMerge().merge(peaFragments.get(0), peaFragments.get(1), mergeTargets);
        String dot = DotWriterNew.createDotString(merged);
        writeFile("/tmp/toCheckPartSequenceMergeTest.dot", dot);
    }

//    @Test
    public void testScopeSequenceMerge() throws Exception {
        final String testString =
                "REQ1_0: Globally, it is always the case that if \"R\" holds for at least \"5\" time units, then \"S\" holds afterwards.\n" +
                "REQ1_1: After \"S\", it is always the case that initially \"termination()\" holds."
                ;
        List<PEAFragment> peaFragments = testPattern2Impl(testString);
//		PEAFragment merged = new SequenceMerge().merge(peaFragments.get(0), peaFragments.get(1));
        PEAFragment merged = new ScopeSequenceMerge().merge(peaFragments.get(0), peaFragments.get(1));
        String dot = DotWriterNew.createDotString(merged);
        writeFile("/tmp/toCheckScopeSequenceMergeTest.dot", dot);
    }



//    @Test
    public void testCompleteMerge() throws Exception {
        final String testString =
                "REQ1_0: Globally, it is always the case that if \"R\" holds for at least \"5\" time units, then \"T\" holds afterwards.\n" +
                "REQ1_1: Globally, it is always the case that if \"R\" holds for at least \"5\" time units, then \"T\" holds afterwards for at least \"10\" time units\n."
                ;

        List<PEAFragment> peaFragments = testPattern2Impl(testString);

        PEAFragment merged = new CompleteMerge().merge(peaFragments.get(0), peaFragments.get(1));
        String dot = DotWriterNew.createDotString(merged);
//		System.out.println(dot);
        writeFile("/tmp/toCheckCompleteMergeTest.dot", dot);
//		System.out.println(merged.dumpJSON());
    }

    @Test
    public void testGeneratePEA() throws Exception {
        final Trace2PeaCompiler compiler =
                new Trace2PeaCompiler(mLogger, new HashSet<String>());

        CDD R = BooleanDecision.create("R");
        CDD S = BooleanDecision.create("S");
        int c1 = 1;
        CounterTrace ct = new CounterTrace(new CounterTrace.DCPhase[]{ phaseT(), phase(R.and(S.negate())), phase(R.negate()), phaseT()});
//				peas.add(new Pair<>(ct, compiler.getResult()));
        PhaseEventAutomata pea = compiler.compile("ct", ct);
        String dot = DotWriterNew.createDotString(pea);
//		System.out.println(dot);
        writeFile("/tmp/toCheckCounterTracePEA.dot", dot);
    }
}
