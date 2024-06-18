package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;


import de.uni_freiburg.informatik.ultimate.lib.pea.*;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

import java.util.*;

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
        if (source.getStateInvariant().implies(target.getStateInvariant())) {
            // 增强target
            source.migratePhantom(target);
        } else if (target.getStateInvariant().implies(source.getStateInvariant())) {
            // target 更强，不需要操作
        } else {
            // 错配
            throw new RuntimeException("migrate phase but not same state invariant\t" + source.getStateInvariant() + "\t" + target.getStateInvariant());
        }

        // outgoing
        for (Transition transition : source.getTransitions()) {
            if (skipDest != null && transition.getDest().compareTo(skipDest) == 0) {
                continue;
            }
            Phase dest = transition.getDest();
            if (dest == source) {
                // 特别的，如果是自循环，加到迁移后的状态上。
                dest = target;
            }
            target.copyTran(dest, transition);
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
            phase.copyTran(target, transition);
        }
        phases.remove(source);
    }

    public static Phase[] findPhasesInReq(ReqDesc desc, PhaseEventAutomata pea, CDD cond, CDD cons) {
        Phase condition = null, afterCond = null, constraint = null;
        Phase[] phases = pea.getPhases();
//        HashSet<Phase> init = new HashSet<>();
//        Collections.addAll(init, pea.getInit());
//        for (Phase phase : pea.getInit()) {
//            init.add(phase.getStateInvariant());
//        }
        if (cond == null) {
            cond = desc.firstCondition();
        }
        if (cons == null) {
            cons = desc.firstConstraint();
        }
        List<Phase> conditions = new ArrayList<>(), constraints = new ArrayList<>();
        for (Phase phase : phases) {
//            System.out.println(phase + "\t" + phase.getStateInvariant());
            if (!phase.getStateInvNoPhantom().isEqual(CDD.TRUE) && (phase.getStateInvNoPhantom().implies(cond) ||
                    cond.implies(phase.getStateInvNoPhantom()))) {
                conditions.add(phase);
            }
            // 注意这里要与 after phase 的命名规则一致。
            if (phase.getName().contains("After_" + cond)) {
                afterCond = phase;
            }

            if (!phase.getStateInvNoPhantom().isEqual(CDD.TRUE) && (phase.getStateInvNoPhantom().implies(cons) ||
                    cons.implies(phase.getStateInvNoPhantom()))) {
                constraints.add(phase);
            }
        }

        condition = choosePhase(desc.getReq().getId(), conditions);
        constraint = choosePhase(desc.getReq().getId(), constraints);


//        if (condition == null || constraint == null) {
//            System.out.println("find Phase not found " + cond + "\t" + cons);
//            for (Phase phase : phases) {
//                System.out.printf(" %s", phase);
//            }
//            System.out.println();
//            for (Phase phase : desc.phases) {
//                System.out.printf(" %s", phase);
//            }
//        }
        return new Phase[]{condition, afterCond, constraint};
    }

    private static Phase choosePhase(String id, List<Phase> phases) {
        Phase result = null;
        result = phases.get(0);
        if (phases.size() > 1) {
            for (Phase phase : phases) {
                if (phase.getName().contains(id)) {
                    result = phase;
                    break;
                }
            }
        }
        return result;
    }


    /**
     * 合并 pea 的 phase, clocks, inits.
     * @param left 输入1
     * @param right 输入2
     * @param phases 结果
     * @param clocks 结果
     * @param inits 结果
     */
    public static void mergeCommon(PEAFragment left, PEAFragment right, Set<Phase> phases, Set<String> clocks, Set<Phase> inits) {
        phases.addAll(List.of(left.getPhases()));
        phases.addAll(List.of(right.getPhases()));

        clocks.addAll(left.getClocks());
        clocks.addAll(right.getClocks());

        inits.addAll(List.of(left.getInit()));
        inits.addAll(List.of(right.getInit()));
    }


    public static PEAFragment mergeMain(HashMap<String, PEAFragment> peas, List<MergeDesc> descs) {
        PEAFragment result = null;
        for (MergeDesc desc : descs) {
            System.out.println("Start merge " + desc.type + " " + desc.leftId + " " + desc.rightId);
            PEAFragment left = peas.get(desc.leftId);
            PEAFragment right = peas.get(desc.rightId);
            if (left == null || right == null) {
                throw new RuntimeException("not found pea");
            }
            PEAFragment merged = null;
            List<CDD> mergeTargets = null;
            StringBuilder targets = new StringBuilder();
            if (desc.mergeTargets != null && !desc.mergeTargets.isEmpty()) {
                mergeTargets = new ArrayList<>();
                for (String target : desc.mergeTargets) {
                    mergeTargets.add(BooleanDecision.create(target));
                    targets.append(target);
                }
                System.out.println("merge target is " + targets);
            }
            switch (desc.type) {
                case "complete":
                    merged = new CompleteMerge().merge(left, right, mergeTargets);
                    break;
                case "condition":
                    merged = new ConditionMerge().merge(left, right, mergeTargets);
                    break;
                case "constraint":
                    merged = new ConstraintMerge().merge(left, right, mergeTargets);
                    break;
                case "sequence":
                    merged = new SequenceMerge().merge(left, right, mergeTargets);
                    break;
                case "part-sequence":
                    merged = new PartSequenceMerge().merge(left, right, mergeTargets);
                    break;
                case "scope-sequence":
                    merged = new ScopeSequenceMerge().merge(left, right, mergeTargets);
                    break;
                default:
                    throw new RuntimeException("unsupported merge type " + desc.type);
            }
            peas.put(desc.leftId, merged);
            peas.put(desc.rightId, merged);
            result = merged;
        }
        // 到终点的都删掉
        Phase dest = result.getDestPhase();
        for (Phase p : result.getPhases()) {
            Transition tr = p.getOutgoingTransition(dest);
            if (tr != null) {
                p.removeTransition(tr);
            }
        }
        Set<Phase> phases = new HashSet<>(Set.of(result.getPhases()));
        phases.remove(dest);
        PEAFragment fragment = new PEAFragment(result.getName(), phases.toArray(new Phase[]{}),
                result.getInit(), result.getClocks());
        Set<ReqDesc> merged = result.getMergedDesc();
        for (ReqDesc desc : merged) {
            fragment.addMergedDesc(desc);
        }
//        result.getDestPhase().setStateInvariant(BooleanDecision.create("termination()"));
        return fragment;
    }
}
