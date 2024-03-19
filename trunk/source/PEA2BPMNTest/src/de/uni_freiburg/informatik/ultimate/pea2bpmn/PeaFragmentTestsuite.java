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

import java.io.StringReader;
import java.util.*;

import com.github.jhoenicke.javacup.runtime.Symbol;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.pea.modelchecking.DotWriterNew;
import de.uni_freiburg.informatik.ultimate.lib.srparse.ReqParser;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.DurationBoundLPattern;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl.PeaImplBuilder;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_freiburg.informatik.ultimate.boogie.BoogieLocation;
import de.uni_freiburg.informatik.ultimate.boogie.DeclarationInformation;
import de.uni_freiburg.informatik.ultimate.boogie.DeclarationInformation.StorageClass;
import de.uni_freiburg.informatik.ultimate.boogie.ExpressionFactory;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.UnaryExpression.Operator;
import de.uni_freiburg.informatik.ultimate.boogie.output.BoogiePrettyPrinter;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogieType;
import de.uni_freiburg.informatik.ultimate.test.mocks.UltimateMocks;

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

	public PeaFragmentTestsuite(final CDD input, final String expected) {
		mInput = input;
		mExpected = expected;
	}


	/**
	 * Use to supply a string (instead of file) to parser.
	 *
	 * @param reqFileName
	 * @return
	 * @throws Exception
	 *
	 */
	private PatternType<?>[] genPatterns(final String testInput) throws Exception {
		final IUltimateServiceProvider services = UltimateMocks.createUltimateServiceProviderMock();

		final StringReader sr = new StringReader(testInput);
		final ReqParser parser = new ReqParser(services.getLoggingService().getLogger(getClass()), sr, "");
		final Symbol goal = parser.parse();
		final PatternType<?>[] patterns = (PatternType[]) goal.value;

		return patterns;
	}

//	@Test
	public void testConditionMerge() throws Exception {
		final String testString =
				"REQ1_0: Globally, it is always the case that once \"R\" becomes satisfied, \"S\" holds for at least \"10\" time units\n" +
				"REQ1_1_1: Globally, it is always the case that once \"R\" becomes satisfied, \"T\" holds after at most \"5\" time units for at least \"10\" time units\n"
				;
		final PatternType<?>[] parsedPatterns = genPatterns(testString);

		ArrayList<PEAFragment> peaFragments = new ArrayList<>();
		for (PatternType<?> pattern : parsedPatterns) {
			peaFragments.add(PeaImplBuilder.build(pattern).generate());
		}

//		Set<Phase> phases = new HashSet<Phase>();
//		Set<String> clocks = new HashSet<String>();
//		Set<Phase> inits = new HashSet<Phase>();
//		peaFragments.get(0).mergeCommon(peaFragments.get(1), phases, clocks, inits);
//		PEAFragment commonMerge = new PEAFragment("comm", phases.toArray(new Phase[]{}),
//				inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
//		System.out.println(DotWriterNew.createDotString(commonMerge));

		PEAFragment merged = peaFragments.get(0).mergeCondition(peaFragments.get(1));
		String dot = DotWriterNew.createDotString(merged);
		System.out.println(dot);
	}


	@Test
	public void testConstraintMerge() throws Exception {
		final String testString =
				"REQ1_0: Globally, it is always the case that once \"R1\" becomes satisfied, \"S\" holds for at least \"10\" time units\n" +
						"REQ1_1_1: Globally, it is always the case that once \"R2\" becomes satisfied, \"S\" holds after at most \"5\" time units for at least \"10\" time units\n"
				;
		final PatternType<?>[] parsedPatterns = genPatterns(testString);

		ArrayList<PEAFragment> peaFragments = new ArrayList<>();
		for (PatternType<?> pattern : parsedPatterns) {
			peaFragments.add(PeaImplBuilder.build(pattern).generate());
		}

//		Set<Phase> phases = new HashSet<Phase>();
//		Set<String> clocks = new HashSet<String>();
//		Set<Phase> inits = new HashSet<Phase>();
//		peaFragments.get(0).mergeCommon(peaFragments.get(1), phases, clocks, inits);
//		PEAFragment commonMerge = new PEAFragment("comm", phases.toArray(new Phase[]{}),
//				inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
//		System.out.println(DotWriterNew.createDotString(commonMerge));

		PEAFragment merged = peaFragments.get(0).mergeConstraint(peaFragments.get(1));
		String dot = DotWriterNew.createDotString(merged);
		System.out.println(dot);
	}

//	@Test
	public void testSequenceMerge() throws Exception {
		final String testString =
				"REQ1_0: Globally, it is always the case that once \"R\" becomes satisfied, \"S\" holds for at least \"10\" time units\n" +
						"REQ1_1_1: Globally, it is always the case that once \"S\" becomes satisfied, \"T\" holds after at most \"5\" time units for at least \"10\" time units\n"
				;
		final PatternType<?>[] parsedPatterns = genPatterns(testString);

		ArrayList<PEAFragment> peaFragments = new ArrayList<>();
		for (PatternType<?> pattern : parsedPatterns) {
			peaFragments.add(PeaImplBuilder.build(pattern).generate());
		}

//		Set<Phase> phases = new HashSet<Phase>();
//		Set<String> clocks = new HashSet<String>();
//		Set<Phase> inits = new HashSet<Phase>();
//		peaFragments.get(0).mergeCommon(peaFragments.get(1), phases, clocks, inits);
//		PEAFragment commonMerge = new PEAFragment("comm", phases.toArray(new Phase[]{}),
//				inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
//		System.out.println(DotWriterNew.createDotString(commonMerge));

		PEAFragment merged = peaFragments.get(0).mergeSequence(peaFragments.get(1));
		String dot = DotWriterNew.createDotString(merged);
		System.out.println(dot);
	}


//	@Test
	public void testCompleteMerge() throws Exception {
		final String testString =
				"REQ1_0: Globally, it is always the case that once \"R\" becomes satisfied, \"S\" holds for at least \"10\" time units\n" +
						"REQ1_1_1: Globally, it is always the case that once \"R\" becomes satisfied, \"S\" holds after at most \"5\" time units for at least \"10\" time units\n"
				;
		final PatternType<?>[] parsedPatterns = genPatterns(testString);

		ArrayList<PEAFragment> peaFragments = new ArrayList<>();
		for (PatternType<?> pattern : parsedPatterns) {
			peaFragments.add(PeaImplBuilder.build(pattern).generate());
		}

//		Set<Phase> phases = new HashSet<Phase>();
//		Set<String> clocks = new HashSet<String>();
//		Set<Phase> inits = new HashSet<Phase>();
//		peaFragments.get(0).mergeCommon(peaFragments.get(1), phases, clocks, inits);
//		PEAFragment commonMerge = new PEAFragment("comm", phases.toArray(new Phase[]{}),
//				inits.toArray(new Phase[]{}), new ArrayList<>(clocks));
//		System.out.println(DotWriterNew.createDotString(commonMerge));

		PEAFragment merged = peaFragments.get(0).mergeComplete(peaFragments.get(1));
		String dot = DotWriterNew.createDotString(merged);
		System.out.println(dot);
	}
}
