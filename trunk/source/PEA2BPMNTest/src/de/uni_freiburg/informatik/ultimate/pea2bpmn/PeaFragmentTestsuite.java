/*
 * Copyright (C) 2018 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2018 University of Freiburg
 *
 * This file is part of the ULTIMATE PEAtoBoogie plug-in.
 *
 * The ULTIMATE PEAtoBoogie plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE PEAtoBoogie plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE PEAtoBoogie plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE PEAtoBoogie plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE PEAtoBoogie plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.pea2bpmn;

import java.util.*;

import com.alibaba.fastjson2.JSON;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.pea.modelchecking.DotWriterNew;
import de.uni_freiburg.informatik.ultimate.lib.pea.modelchecking.J2UPPAALWriterV4;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge.*;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import de.uni_freiburg.informatik.ultimate.test.mocks.UltimateMocks;

import static de.uni_freiburg.informatik.ultimate.pea2bpmn.PeaTestUtil.*;


/**
 *
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 */
@RunWith(Parameterized.class)
public class PeaFragmentTestsuite {

	@Parameters
	public static Collection<Object[]> data() {
		final CDD A = CDD.create(new BooleanDecision("A"), CDD.TRUE_CHILDS);
		final CDD B = CDD.create(new BooleanDecision("B"), CDD.TRUE_CHILDS);
		final CDD C = CDD.create(new BooleanDecision("C"), CDD.TRUE_CHILDS);
//		final CDD notC = BoogieBooleanExpressionDecision.createWithoutReduction(
//				ExpressionFactory.constructUnaryExpression(LOC, Operator.LOGICNEG, new IdentifierExpression(LOC,
//						BoogieType.TYPE_BOOL, "C", new DeclarationInformation(StorageClass.GLOBAL, null))));
//		final CDD notCCreate = BoogieBooleanExpressionDecision
//				.create(ExpressionFactory.constructUnaryExpression(LOC, Operator.LOGICNEG, new IdentifierExpression(LOC,
//						BoogieType.TYPE_BOOL, "C", new DeclarationInformation(StorageClass.GLOBAL, null))));

		final CDD eqAZero = RangeDecision.create("A", RangeDecision.OP_EQ, 0);
		final CDD gtAOne = RangeDecision.create("A", RangeDecision.OP_GT, 1);
		final CDD ltAZero = RangeDecision.create("A", RangeDecision.OP_LT, 0);
		final CDD geqAOne = RangeDecision.create("A", RangeDecision.OP_GTEQ, 1);

		//@formatter:off
		return Arrays.asList(new Object[][] {
			{ CDD.TRUE, "true" },
//			{ CDD.FALSE, "false" },
//			{ eqAZero, "A == 0.0" },
//			{ eqAZero.negate(), "A > 0.0 || A < 0.0" },
//			{ eqAZero.and(gtAOne), "false" },
//			{ gtAOne, "A > 1.0" },
//			{ gtAOne.negate(), "A <= 1.0" },
//			{ geqAOne, "A >= 1.0" },
//			{ ltAZero, "A < 0.0" },
//			{ ltAZero.negate(), "A >= 0.0" },
//			{ ltAZero.and(gtAOne.negate()), "A < 0.0" },
//			{ ltAZero.or(eqAZero), "A <= 0.0" },
//			{ ltAZero.negate().and(gtAOne.negate()), "0.0 <= A && A <= 1.0" },
//			{ ltAZero.and(B).or(gtAOne.negate()), "A <= 1.0" },
//			{ ltAZero.and(B.or(gtAOne.negate())), "A < 0.0" },
//			{ ltAZero.or(B.and(gtAOne.negate())), "A < 0.0 || (B && A <= 1.0)" },
//			{ geqAOne.negate().and(ltAZero.or(eqAZero).negate()), "0.0 < A && A < 1.0" },
//			{ A.and(B).or(B.and(A.negate())), "B" },
//			{ A.and(B).or(B.negate().and(A)), "A" },
//			{ A, "A" },
//			{ B, "B" },

//			{ notC, "!C" },
//			{ notC.and(notC.negate()), "false" },
//			{ C.and(notC), "!C && C" },
//			{ C.and(notCCreate), "false" },

//			{ A.negate(), "!A" },
//			{ A.or(B), "B || A" },
//			{ A.and(B), "A && B" },
//			{ A.and(B.negate()), "A && !B" },
//			{ A.negate().and(B), "!A && B" },
//			{ A.negate().and(B.negate()), "!A && !B" },
//			{ A.negate().and(B.negate()).negate(), "B || A" },

		});
		//@formatter:on
	}

	private final CDD mInput;
	private final String mExpected;
	private IUltimateServiceProvider mServices = UltimateMocks.createUltimateServiceProviderMock();
	private ILogger mLogger;

	public PeaFragmentTestsuite(final CDD input, final String expected) {
		mInput = input;
		mExpected = expected;
		mLogger = mServices.getLoggingService().getLogger(PeaFragmentTestsuite.class);
	}


//	@Test
	public void testCaseSpec2Uppaal() throws Exception {
//		final String reqString1 =
//			"REQ1_10_10: Globally, it is always the case that once \"R\" becomes satisfied, it holds for less than \"8\" time units\n" +
//			"REQ1_11_11: Globally, it is always the case that once \"R\" becomes satisfied, it holds for at least \"5\" time units"
//				;
//		final String reqString =
//				"Req1: Before \"_end\", it is always the case that if \"A\" holds, then \"B&&C\" eventually holds.\n" +
//						"Req2: After \"A\", it is always the case that if \"B\" holds, then \"D||E\" holds after at most \"10\" time units.\n" +
//						"Req3_1: Before \"_end\", it is always the case that if \"D\" holds, then \"F\" eventually holds.\n" +
//						"Req3_2: Before \"_end\", it is always the case that if \"E\" holds, then \"F\" eventually holds.\n" +
//						"Req4: Globally, it is always the case that if \"C&&F\" holds for at least \"5\" time units, then \"G\" holds afterwards. "
//		;

		final String reqString =
				"Req1: Globally, it is always the case that if \"A\" holds, then \"B&&C\" holds after at most \"10\" time units.\n" +
						"Req2: After \"A\", it is always the case that if \"B\" holds, then \"D||E\" holds after at most \"10\" time units.\n" +
						"Req3_1: Globally, it is always the case that if \"D\" holds, then \"F\" holds after at most \"10\" time units.\n" +
						"Req3_2: Globally, it is always the case that if \"E\" holds, then \"F\" holds after at most \"10\" time units.\n" +
						"Req4: Globally, it is always the case that if \"C&&F\" holds for at least \"5\" time units, then \"G\" holds afterwards."
				;
		/**
 		 */
		final String reqString2 =
//				"Req4_8: Globally, it is always the case that initially \"!choose_year_month\" holds.\n"
//			+	"Req4_9: Globally, it is always the case that if \"choose_year_month\" holds, then \"order_cash_report\" holds after at most \"10\" time units.\n"
//			+   "Req4_10: After \"order_cash_report\", it is always the case that \"filter_cash_transactions\" holds after at most \"10\" time units.\n"
//			+	"Req4_10_2: After \"order_cash_report\", it is always the case that \"#start_filter\" holds after at most \"10\" time units.\n"
//			+	"Req4_11: Globally, it is always the case that once \"filter_cash_transactions\" becomes satisfied, it holds for less than \"5\" time units.\n"
//			+	"Req4_11_2: Globally, it is always the case that if \"#start_filter\" holds, then \"#end_filter\" holds after at most \"10\" time units.\n"
//			+   "Req4_12: Globally, it is always the case that if \"filter_cash_transactions\" holds then \"query_not_empty || query_empty\" eventually holds.\n"
//			+	"Req4_12_2: Globally, it is always the case that if \"#end_filter\" holds then \"query_not_empty || query_empty\" eventually holds.\n"
//			+   "Req4_13: After \"query_not_empty\", it is always the case that initially \"print_cash_report\" holds.\n"
//			+   "Req4_14: Globally, it is always the case that if \"query_empty||query_not_empty\" holds, then \"send_report_result\" eventually holds.\n"
			 	"Req4_15: Between \"#start_filter\" and \"#end_filter\", it is always the case that initially \"all(filter_credit_card, filter_normal_card, send(apply_special_card_permissions#wakeup))\" holds.\n"
			+ 	"Req4_16: Globally, it is always the case that if \"receive(apply_special_card_permissions)\" holds, then \"approve_permissions||deny_permissions\" eventually holds.\n"
			+ 	"Req4_17: After \"approve_permissions\", it is always the case that initially \"send(grant_permissions#sticky)\" holds.\n"
			+   "Req4_18: After \"deny_permissions\", it is always the case that initially \"send(not_grant_permissions)\" holds.\n"
			+	"Req4_19: Between \"#start_filter\" and \"#end_filter\", it is always the case that if \"receive(grant_permissions)\" holds, then \"filter_special_card\" holds after at most \"10\" time units.\n"
				;
		final String reqString6 =
				"Req6_1: Globally, it is always the case that initially \"!receives_an_order\" holds.\n" +
				"Req6_2: Globally, it is always the case that if \"receives_an_order\" holds then \"accept_order || reject_order\" eventually holds.\n" +
				"Req6_3: After \"reject_order\", it is always the case that initially \"termination()\" holds.\n" +
				"Req6_4: After \"accept_order\", it is always the case that initially \"storehouse_start? && engineering_start?\" holds.\n" +
				"Req6_5: After \"storehouse_start?\", it is always the case that initially \"process_part_list\" holds.\n" +
				"Req6_6: Globally, it is always the case that if \"process_part_list\" holds then \"reserved_parts || back_ordered_parts\" eventually holds.\n" +
				"Req6_7: After \"reserved_parts || back_ordered_parts\", it is always the case that initially \"engineering_parts_ready? || process_part_list\" holds.\n" +
				"Req6_8: After \"engineering_start?\", it is always the case that initially \"prepares_assemble_bicycle\" holds.\n"
//				"Req6_9: Globally, it is always the case that \"criticalSession(reserved_parts,prepares_assemble_bicycle)\" holds\n" +
				+ "Req6_10: After \"engineering_parts_ready? && prepares_assemble_bicycle\", it is always the case that \"assemble_bicycle\" holds after at most \"10\" time units.\n"
				+ "Req6_11: After \"assemble_bicycle\", it is always the case that \"sales_bicycle_assembled?\" holds after at most \"10\" time units.\n"
				+ "Req6_12: Globally, it is always the case that if \"sales_bicycle_assembled?\" holds, then \"ship_bicycle_to_customer\" eventually holds and is succeeded by \"termination()\".\n"
//				+ "Req6_13: Globally, it is always the case that if \"accept_order\" holds, then \"ship_bicycle_to_customer\" holds after at most \"4320\" time units.\n"
		;
		final J2UPPAALWriterV4 j2uppaalWriter = new J2UPPAALWriterV4();
//		PhaseEventAutomata pea = req2PEA(reqString6);
//		j2uppaalWriter.writePEA2UppaalFile("/tmp/toCheck" + "CaseTest" + ".xml", pea);

//		final String dotTargenPatternsget = "/tmp/toCheckManual.dot";
		PhaseEventAutomata gen = new HandWritingGen().generateHandWritingPEACh6(new String[]{}, mLogger);
//		String dotS = DotWriterNew.createDotString(gen);
//		mLogger.info("\n\n\ndotS========================\n\n\n");
//		mLogger.info(dotS);
//		String dotS2 = DotWriterNew.createDotString(pea);
//		mLogger.info(dotS2);

		j2uppaalWriter.writePEA2UppaalFile("/tmp/toCheck" + "CaseManual" + ".xml", gen);


	}

	@Test
	public void testReqSetMerge() throws Exception {
//		final String testString =
//				"Req3_8: Globally, it is always the case that initially \"!choose_year_month\" holds\n" +
//				"Req3_9: Globally, it is always the case that if \"choose_year_month\" holds, then \"order_cash_report\" eventually holds\n" +
//				"Req3_10: Globally, it is always the case that if \"order_cash_report\" holds, then \"filter_cash_transactions\" eventually holds\n" +
//				"Req3_11: Globally, it is always the case that once \"filter_cash_transactions\" becomes satisfied, it holds for less than \"5\" time units\n" +
//				"Req3_12: Globally, it is always the case that if \"filter_cash_transactions\" holds,  then \"query_not_empty || query_empty\" eventually holds\n" +
//				"Req3_13: Globally, it is always the case that if \"query_not_empty\" holds,  then \"print_cash_report\" eventually holds\n" +
//				"Req3_14: Globally, it is always the case that if \"query_empty||print_cash_report\" holds, then \"send_report_result\" eventually holds\n" +
//
//				"ReqT: Globally, it is always the case that if \"send_report_result\" holds, then \"termination()\" eventually holds"
//				;
		final String testString =
				"Req6_1: Globally, it is always the case that initially \"!receives_an_order\" holds.\n" +
						"Req6_2: Globally, it is always the case that if \"receives_an_order\" holds then \"accept_order || reject_order\" eventually holds.\n" +
						"Req6_3: After \"reject_order\", it is always the case that initially \"termination()\" holds.\n" +
						"Req6_4: After \"accept_order\", it is always the case that initially \"#storehouse_start && #engineering_start\" holds.\n" +
						"Req6_5: After \"#storehouse_start\", it is always the case that initially \"process_part_list\" holds.\n" +
						"Req6_6: Globally, it is always the case that if \"process_part_list\" holds then \"reserved_parts || back_ordered_parts\" eventually holds.\n" +
						"Req6_7: After \"reserved_parts || back_ordered_parts\", it is always the case that initially \"#engineering_parts_ready || process_part_list\" holds.\n" +
						"Req6_8: After \"#engineering_start\", it is always the case that initially \"prepares_assemble_bicycle\" holds.\n" +
						"Req6_9: Globally, it is always the case that \"criticalSession(reserved_parts, prepares_assemble_bicycle)\" holds.\n" +
						"Req6_10: Globally, it is always the case that if \"#engineering_parts_ready && prepares_assemble_bicycle\" holds, then \"assemble_bicycle\" holds after at most \"10\" time units.\n" +
						"Req6_11: After \"assemble_bicycle\", it is always the case that \"#sales_bicycle_assembled\" holds after at most \"10\" time units.\n" +
						"Req6_12: Globally, it is always the case that if \"#sales_bicycle_assembled\" holds, then \"ship_bicycle_to_customer\" eventually holds.\n" +
						"ReqT: Globally, it is always the case that if \"ship_bicycle_to_customer\" holds, then \"termination()\" eventually holds"
				;

		List<PEAFragment> peaFragments = testPattern2Impl(testString);
//		assert peaFragments.size() == 3;
//		String dot0 = DotWriterNew.createDotString(peaFragments.get(0));
//		writeFile("/tmp/toCheckSetMergeStep0.dot", dot0);
//		PEAFragment merged1 = new ConstraintMerge().merge(peaFragments.get(1), peaFragments.get(2));
//		String dot1 = DotWriterNew.createDotString(merged1);
//		writeFile("/tmp/toCheckSetMergeStep1.dot", dot1);
//		PEAFragment merged2 = new SequenceMerge().merge(peaFragments.get(0), merged1, List.of(BooleanDecision.create("D")));
//		String dot2 = DotWriterNew.createDotString(merged2);
//		writeFile("/tmp/toCheckSetMergeStep2.dot", dot2);
//		PEAFragment merged3 = new SequenceMerge().merge(merged2, merged2, List.of(BooleanDecision.create("E")));
//		String dot3 = DotWriterNew.createDotString(merged3);
//		writeFile("/tmp/toCheckSetMergeStep3.dot", dot3);
//		System.out.println(merged1.dumpJSON());
//		System.out.println(merged2.dumpJSON());
//		System.out.println(merged3.dumpJSON());

		HashMap<String, PEAFragment> id_pea_map = new HashMap<String, PEAFragment>();

		for (PEAFragment pea : peaFragments) {
			id_pea_map.put(pea.getDesc().getReq().getId(), pea);
		}

		List<MergeDesc> descs2 = JSON.parseArray("[\n" +
				"  {\n" +
				"    \"type\": \"scope-sequence\",\n" +
				"    \"leftId\": \"Req6_2\",\n" +
				"    \"rightId\": \"Req6_3\",\n" +
				"    \"mergeTargets\": [\"reject_order\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"scope-sequence\",\n" +
				"    \"leftId\": \"Req6_2\",\n" +
				"    \"rightId\": \"Req6_4\",\n" +
				"    \"mergeTargets\": [\"accept_order\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"scope-sequence\",\n" +
				"    \"leftId\": \"Req6_4\",\n" +
				"    \"rightId\": \"Req6_5\",\n" +
				"    \"mergeTargets\": [\"#storehouse_start\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"sequence\",\n" +
				"    \"leftId\": \"Req6_5\",\n" +
				"    \"rightId\": \"Req6_6\",\n" +
				"    \"mergeTargets\": [\"process_part_list\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"scope-sequence\",\n" +
				"    \"leftId\": \"Req6_6\",\n" +
				"    \"rightId\": \"Req6_7\",\n" +
				"    \"mergeTargets\": [\"reserved_parts\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"scope-sequence\",\n" +
				"    \"leftId\": \"Req6_6\",\n" +
				"    \"rightId\": \"Req6_7\",\n" +
				"    \"mergeTargets\": [\"back_ordered_parts\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"scope-sequence\",\n" +
				"    \"leftId\": \"Req6_4\",\n" +
				"    \"rightId\": \"Req6_8\",\n" +
				"    \"mergeTargets\": [\"#engineering_start\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"part-sequence\",\n" +
				"    \"leftId\": \"Req6_7\",\n" +
				"    \"rightId\": \"Req6_10\",\n" +
				"    \"mergeTargets\": [\"#engineering_parts_ready\"]\n" +
				"  },\n" +
				"\n" +
				"  {\n" +
				"    \"type\": \"part-sequence\",\n" +
				"    \"leftId\": \"Req6_7\",\n" +
				"    \"rightId\": \"Req6_5\",\n" +
				"    \"mergeTargets\": [\"process_part_list\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"part-sequence\",\n" +
				"    \"leftId\": \"Req6_8\",\n" +
				"    \"rightId\": \"Req6_10\",\n" +
				"    \"mergeTargets\": [\"prepares_assemble_bicycle\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"scope-sequence\",\n" +
				"    \"leftId\": \"Req6_10\",\n" +
				"    \"rightId\": \"Req6_11\",\n" +
				"    \"mergeTargets\": [\"assemble_bicycle\"]\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"sequence\",\n" +
				"    \"leftId\": \"Req6_11\",\n" +
				"    \"rightId\": \"Req6_12\",\n" +
				"    \"mergeTargets\": [\"#sales_bicycle_assembled\"]\n" +
				"  }\n" +
				"]", MergeDesc.class);
		PEAFragment result = PairMergerUtils.mergeMain(id_pea_map, descs2);
		String dot4 = DotWriterNew.createDotString(result);
		writeFile("/tmp/toCheckSetMergeAllInOne.dot", dot4);
		System.out.println(result.dumpJSON());
	}

	public void testMergeCommon(PEAFragment left, PEAFragment right) {
		Set<Phase> phases = new HashSet<>();
		Set<String> clocks = new HashSet<>();
		Set<Phase> inits = new HashSet<>();
		PairMergerUtils.mergeCommon(left, right, phases, clocks, inits);
		PEAFragment commonMerge = new PEAFragment("comm", phases.toArray(new Phase[]{}),
				inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
		System.out.println(DotWriterNew.createDotString(commonMerge));
	}

//	@Test
	public void testAllCompleteMerge() throws Exception {
		final String[] hanforArr = new String[]{
//				"REQ1_0: Globally, it is always the case that once \"%s\" becomes satisfied, \"%s\" holds for at least \"10\" time units\n",
//				"REQ1_1_1: Globally, it is always the case that once \"%s\" becomes satisfied, \"%s\" holds after at most \"5\" time units for at least \"10\" time units\n",
//				"REQ1_2: Globally, it is always the case that if \"%s\" holds for at least \"3\" time units, then \"%s\" holds afterwards\n",
//				"REQ1_3: Globally, it is always the case that if \"%s\" holds for at least \"4\" time units, then \"%s\" holds afterwards for at least \"8\" time units\n",
				// Not tested
//				"REQ1_4: Globally, it is always the case that if \"%s\" holds for at least \"6\" time units, then \"%s\" holds after at most \"10\" time units\n",
//				"REQ1_5: Globally, it is always the case that if \"%s\" holds, then \"%s\" holds after at most \"5\" time units for at least \"10\" time units"
				"REQ1_0: Globally, it is always the case that if \"%s\" holds for at least \"5\" time units, then \"%s\" holds afterwards.\n",
				"REQ1_1: Globally, it is always the case that if \"%s\" holds for at least \"5\" time units, then \"%s\" holds afterwards for at least \"10\" time units."
		};


		int id = 0;
		String[][] direction = new String[][]{
				{"R","S"},
				{"R","T"},
				{"T","S"},
				{"S","T"}};
		for (int i = 0; i < hanforArr.length; i++) {
			for (int j = i+1; j < hanforArr.length; j++) {
				for (int k = 0; k < direction.length; k++) {
					System.out.println("填充前规约Req1：\n" + hanforArr[i]);
					String req1 = String.format(hanforArr[i], "R", "S");
					System.out.println("填充前规约Req2：\n" + hanforArr[j]);
					String req2 = String.format(hanforArr[j], direction[k][0], direction[k][1]);
					System.out.println("填充后规约：\n" + req1 + "\n" + req2);
					String testString = req1 + req2;
					merge(testString, k, id++);
				}

			}
		}
	}

	public static void merge(String testString, int type, int id) throws Exception {
		List<PEAFragment> peaFragments = testPattern2Impl(testString);
		IPeaMerger merger;
		switch (type) {
			case 0:
				merger = new CompleteMerge();
				break;
			case 1:
				merger = new ConditionMerge();
				break;
			case 2:
				merger = new ConstraintMerge();
				break;
			case 3:
				merger = new SequenceMerge();
				break;
			default:
				System.out.println("unsupport merger type");
				return;
		}
		PEAFragment merged = merger.merge(peaFragments.get(0), peaFragments.get(1));
		if (merged == null) {
			System.out.println(testString + "\t merged Failed");
			return;
		}
		String dot = DotWriterNew.createDotString(merged);
//		System.out.println(dot);

		writeFile("/tmp/toCheckFullMerge"+ id+ ".dot", dot);
		// System.out.println("-----------");
//		System.out.println(merged.dumpJSON());
	}
}
