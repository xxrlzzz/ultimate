package de.uni_freiburg.informatik.ultimate.pea2bpmn;

import java.util.*;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.pea.modelchecking.DotWriterNew;

public class HandWritingGen {
    /**
     * 手搓的生成
     * @return
     */
    public PhaseEventAutomata generateHandWritingPEA(ILogger logger) {
        final String id = "impl1";
        final CDD B = BooleanDecision.create("B");
        final CDD C = BooleanDecision.create("C");
        final CDD f = BooleanDecision.create("f");
//		final int c1 = SmtUtils.toInt(mReq.getDurations().get(0)).intValueExact();

        String bClock = B + "t";
        String cClock = C + "t";
        String fClock = f + "t";
//		CDD constraintDr = RangeDecision.create(rClock, RangeDecision.OP_LT, c1);

        Phase pA = new Phase(id + "_stA", BooleanDecision.create("A"), CDD.TRUE);
        Phase pA2 = new Phase(id + "_stA2", BooleanDecision.create("A"), CDD.TRUE);

        Phase pB = new Phase(id + "_stB", BooleanDecision.create("B"), CDD.TRUE);
        Phase pC = new Phase(id + "_stC", BooleanDecision.create("C"), CDD.TRUE);
        Phase pD = new Phase(id + "_stD", BooleanDecision.create("D"), CDD.TRUE);
        Phase pE = new Phase(id + "_stE", BooleanDecision.create("E"), CDD.TRUE);
        Phase pF = new Phase(id + "_stF", BooleanDecision.create("F"), CDD.TRUE);
        Phase pG = new Phase(id + "_stG", BooleanDecision.create("G"), CDD.TRUE);
        Phase pG2 = new Phase(id + "_stG2", BooleanDecision.create("G"), CDD.TRUE);

        pA.addTransition(pB, CDD.TRUE, new String[]{bClock});
        pA2.addTransition(pC, CDD.TRUE, new String[]{cClock});
        pB.addTransition(pD, RangeDecision.create(bClock, RangeDecision.OP_LTEQ, 10), new String[]{});
        pB.addTransition(pE, RangeDecision.create(bClock, RangeDecision.OP_LTEQ, 10), new String[]{});
        pD.addTransition(pF, CDD.TRUE, new String[]{fClock});
        pE.addTransition(pF, CDD.TRUE, new String[]{fClock});
        pF.addTransition(pG, RangeDecision.create(fClock, RangeDecision.OP_LTEQ, 5), new String[]{});
        pC.addTransition(pG2, CDD.TRUE, new String[]{});
        String peaName = id;
        PhaseEventAutomata pea = new PhaseEventAutomata(peaName, new Phase[]{pA,pB,pD,pE,pF,pG}, new Phase[]{pA},
                List.of(bClock,cClock,fClock));

        PhaseEventAutomata pea2 = new PhaseEventAutomata(peaName, new Phase[]{pA2,pC,pG2}, new Phase[]{pA2},
                List.of(cClock));

        logger.info("\n\n\ndotS========================\n\n\n");
        logger.info(DotWriterNew.createDotString(pea));
        logger.info(DotWriterNew.createDotString(pea2));
//		return pea.parallel(pea2);
        return pea;
    }

    public PhaseEventAutomata generateHandWritingPEACh6(String[] clocks, ILogger logger) {
        final String id = "impl1";
        final CDD req6_1_st1 = BooleanDecision.create("receives_an_order");
        final CDD req6_2_st1 = BooleanDecision.create("accept_order");
        final CDD req6_2_st2 = BooleanDecision.create("reject_order");
        final CDD req6_4_st1 = BooleanDecision.create("message(start)");
        final CDD req6_13_st3 = BooleanDecision.create("After_Rise_accept_order");
        final CDD req6_5_st1 = BooleanDecision.create("process_part_list");
        final CDD req6_8_st1 = BooleanDecision.create("prepare_assemble_bicycle");
        final CDD req6_6_st2 = BooleanDecision.create("reserved_parts || back_order_parts");
        final CDD req6_10_st1 = BooleanDecision.create("all");
        final CDD req6_10_st2 = BooleanDecision.create("assemble_bicycle");
        final CDD req6_11_st2 = BooleanDecision.create("message(bicycle_assembled)");
        final CDD req6_12_st2 = BooleanDecision.create("ship_bicycle_to_customer");
        final CDD req6_3_st1 = BooleanDecision.create("_termination");


//		String bClock = B + "t";
//		String cClock = C + "t";
//		String fClock = f + "t";
//		CDD constraintDr = RangeDecision.create(rClock, RangeDecision.OP_LT, c1);

        Phase p6_1_st1 = new Phase(id + "_6_1_st1", req6_1_st1, CDD.TRUE);
        Phase p6_2_st1 = new Phase(id + "_6_2_st1", req6_2_st1, CDD.TRUE);
        Phase p6_2_st2 = new Phase(id + "_6_2_st2", req6_2_st2, CDD.TRUE);
        Phase p6_4_st1 = new Phase(id + "_6_4_st1", req6_4_st1, CDD.TRUE);
        Phase p6_13_st3 = new Phase(id + "_6_13_st3", req6_13_st3, CDD.TRUE);
        Phase p6_5_st1 = new Phase(id + "_6_5_st1", req6_5_st1, CDD.TRUE);
        Phase p6_8_st1 = new Phase(id + "_6_8_st1", req6_8_st1, CDD.TRUE);
        Phase p6_6_st2 = new Phase(id + "_6_6_st2", req6_6_st2, CDD.TRUE);
        Phase p6_10_st1 = new Phase(id + "_6_10_st1", req6_10_st1, CDD.TRUE);
        Phase p6_10_st2 = new Phase(id + "_6_10_st2", req6_10_st2, CDD.TRUE);
        Phase p6_11_st2 = new Phase(id + "_6_11_st2", req6_11_st2, CDD.TRUE);
        Phase p6_12_st2 = new Phase(id + "_6_12_st2", req6_12_st2, CDD.TRUE);
        Phase p6_3_st1 = new Phase(id + "_6_3_st1", req6_3_st1, CDD.TRUE);

        p6_1_st1.addSelfTrans();
        p6_4_st1.addSelfTrans();
        p6_5_st1.addSelfTrans();
        p6_8_st1.addSelfTrans();
        p6_6_st2.addSelfTrans();
        p6_10_st2.addSelfTrans();
        p6_11_st2.addSelfTrans();
        p6_12_st2.addSelfTrans();
        p6_3_st1.addSelfTrans();


        p6_13_st3.addSelfTrans();
        p6_10_st1.addSelfTrans();
        p6_2_st1.addSelfTrans();
        p6_2_st2.addSelfTrans();

//		p6_10_st1.addTransition(p6_10_st1, )
        String[] emptyReset = new String[]{};

        p6_1_st1.addTransition(p6_2_st1, CDD.TRUE, emptyReset);
        p6_1_st1.addTransition(p6_2_st2, CDD.TRUE, emptyReset);
        p6_2_st1.addTransition(p6_4_st1, CDD.TRUE, emptyReset);
        p6_2_st1.addTransition(p6_13_st3, CDD.TRUE, emptyReset);
        p6_13_st3.addTransition(p6_12_st2, CDD.TRUE, emptyReset);
        p6_4_st1.addTransition(p6_5_st1, CDD.TRUE, emptyReset);
        p6_4_st1.addTransition(p6_8_st1, CDD.TRUE, emptyReset);
        p6_8_st1.addTransition(p6_10_st1, CDD.TRUE, emptyReset);
        p6_5_st1.addTransition(p6_6_st2, CDD.TRUE, emptyReset);
        p6_6_st2.addTransition(p6_5_st1, CDD.TRUE, emptyReset);
        p6_6_st2.addTransition(p6_10_st1, CDD.TRUE, emptyReset);
        p6_10_st1.addTransition(p6_10_st2, CDD.TRUE, emptyReset);
        p6_10_st2.addTransition(p6_11_st2, CDD.TRUE, emptyReset);
        p6_11_st2.addTransition(p6_12_st2, CDD.TRUE, emptyReset);
        p6_12_st2.addTransition(p6_3_st1, CDD.TRUE, emptyReset);
        p6_2_st2.addTransition(p6_3_st1, CDD.TRUE, emptyReset);

        String peaName = id;
        PhaseEventAutomata pea = new PhaseEventAutomata(peaName, new Phase[]{p6_1_st1,p6_2_st1,p6_2_st2,p6_4_st1,
                p6_13_st3,p6_5_st1,p6_8_st1, p6_6_st2,p6_10_st1,p6_10_st2,p6_11_st2,p6_12_st2,p6_3_st1}, new Phase[]{p6_1_st1},
                List.of());
//		PhaseEventAutomata pea = new PhaseEventAutomata(peaName, new Phase[]{pA,pB,pD,pE,pF,pG}, new Phase[]{pA},
//				List.of(bClock,cClock,fClock));

//		PhaseEventAutomata pea2 = new PhaseEventAutomata(peaName, new Phase[]{pA2,pC,pG2}, new Phase[]{pA2},
//				List.of(cClock));

        logger.info("\n\n\ndotS========================\n\n\n");
        logger.info(DotWriterNew.createDotString(pea));
//		mLogger.info(DotWriterNew.createDotString(pea2));
//		return pea.parallel(pea2);
        return pea;
    }

}
