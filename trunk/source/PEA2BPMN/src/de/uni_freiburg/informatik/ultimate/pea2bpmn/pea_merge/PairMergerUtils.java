package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;


import de.uni_freiburg.informatik.ultimate.lib.pea.Phase;
import de.uni_freiburg.informatik.ultimate.lib.pea.Transition;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.List;
import java.util.Set;

public class PairMergerUtils {
    public static PEAFragment merge(PEAFragment left, PEAFragment right) {
        ReqDesc.ReqOverlapping comRes = left.getDesc().compare(right.getDesc());
        switch (comRes) {
            case Full:
            case Head:
            case Tail:
            case HeadTail:
            case Loop:
                return null;
            case TwoParts:
                return null;
            case None:
                return null;
        }
        return null;
    }


    // check there has any incoming transition
    public static boolean assert_no_incoming = false;

    /**
     * merge phase from source to target
     * 1. outgoing transitions
     * 2. incoming transitions
     * 3. delete the phase
     * @param source
     * @param target
     */
    public static void migratePhase(Phase source, Phase target, Set<Phase> phases, Phase skipDest) {
        if (!source.getStateInvariant().isEqual(target.getStateInvariant())) {
            throw new RuntimeException("migrate phase but not same state invariant\t" + source.getStateInvariant() + "\t" + target.getStateInvariant());
        }

        // outgoing
        for (Transition transition : source.getTransitions()) {
            if (skipDest != null && transition.getDest().compareTo(skipDest) == 0) {
                continue;
            }
            Transition nt = target.addTransition(transition.getDest(), transition.getGuard(), transition.getResets());
            transition.getClockWriter().forEach((c, t) -> {
                // 先不考虑 clock writer 冲突的情况
                nt.putClockWriter(c, t);
            });
        }
        // incoming
        for (Phase phase : phases) {
            Transition transition = phase.getOutgoingTransition(source);
            if (transition == null) continue;
            phase.removeTransition(transition);
            if (assert_no_incoming) {
                assert_no_incoming = false;
                throw new RuntimeException("expect no incoming transition but has");
            }
            Transition maybeT = phase.getOutgoingTransition(target);
            if (maybeT != null) {
                // 已经有到 target 的边了，这里选择升级老边
                maybeT.setGuard(maybeT.getGuard().and(transition.getGuard()));
                // TODO: merge transition
                continue;
            }
            Transition nt = phase.addTransition(target, transition.getGuard(), transition.getResets());
            transition.getClockWriter().forEach((c, t) -> {
                nt.putClockWriter(c, t);
            });
        }
        phases.remove(source);
    }

    public static Phase[] findPhasesInReq(ReqDesc desc, Phase[] phases) {
        Phase condition = null, afterCond = null, constraint = null;
        for (Phase phase : phases) {
//            if (desc.firstCondition() == null || desc.firstCondition().getDecision() == null) {
//                throw new RuntimeException("empty desc first conditon " + desc.getReq());
//            }
//            if (phase.getStateInvariant() == null || phase.getStateInvariant().getDecision() == null) {
//                throw new RuntimeException("empty phase: " + phase + "\t"+ phase.getStateInvariant());
//            }
            if (phase.getStateInvariant().isEqual(desc.firstCondition())) {
                condition = phase;
            }
            // TODO: fix after cond find
            if (phase.getName().contains("After")) {
                afterCond = phase;
            }
            if (phase.getStateInvariant().isEqual(desc.firstConstraint())) {
                constraint = phase;
            }
        }
        return new Phase[]{condition, afterCond, constraint};
    }


    public static void mergeCommon(PEAFragment left, PEAFragment right, Set<Phase> phases, Set<String> clocks, Set<Phase> inits) {
        phases.addAll(List.of(left.getPhases()));
        phases.addAll(List.of(right.getPhases()));

        clocks.addAll(left.getClocks());
        clocks.addAll(right.getClocks());

        inits.addAll(List.of(left.getInit()));
        inits.addAll(List.of(right.getInit()));
    }

}
