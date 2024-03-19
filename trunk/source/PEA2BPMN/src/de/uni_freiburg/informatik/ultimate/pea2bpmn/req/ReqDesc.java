package de.uni_freiburg.informatik.ultimate.pea2bpmn.req;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.RangeDecision;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;

import java.util.List;

public class ReqDesc {
    private PatternType<?> originReq;
    private List<CDD> conditionVars;
    private List<CDD> constraintVars;
    public CDD conditionDuration;
    public CDD constraintDelay;
    public CDD constraintDuration;

    public ReqDesc(PatternType<?> req, List<CDD> conditions, List<CDD> constraints,
                   CDD conditionDr, CDD constraintDl, CDD constraintDr) {
        originReq = req;
        assert conditions != null && !conditions.isEmpty();
        assert constraints != null && !constraints.isEmpty();
        conditionVars = conditions;
        constraintVars = constraints;
        conditionDuration = conditionDr;
        constraintDelay = constraintDl;
        constraintDuration = constraintDr;
    }

    public enum ReqOverlapping {
        None, // none of it same
        TwoParts, // (I_a = I_b & C_a = I_b) or (I_a = C_b & C_a = C_b)
        Loop, // I_a = C_b & C_a = I_b,
        HeadTail, // 顺序合并， I_a = C_b or I_b = C_a
        Full, // 完全合并， I_a = I_b & C_a = C_b
        Tail, // 控制合并， C_a = C_b
        Head, // 输入合并， I_a = I_b
    }

    public enum DurationOverlapping {

    }

    public boolean multiConstraint() {
        return constraintVars.size() > 1;
    }

    public boolean multiCondition() {
        return conditionVars.size() > 1;
    }

    public CDD firstCondition() {
        return conditionVars.get(0);
    }

    public CDD secondCondition() {
        return conditionVars.get(1);
    }

    public CDD firstConstraint() {
        return constraintVars.get(0);
    }

    public ReqOverlapping compare(ReqDesc oth) {
        // 先不考虑多条件
        if (oth.multiCondition() || oth.multiConstraint()) {
            return ReqOverlapping.None;
        }
        if (multiConstraint() || multiCondition()) {
            return ReqOverlapping.None;
        }

        CDD othCond = oth.conditionVars.get(0);
        CDD othCons = oth.constraintVars.get(0);
        CDD selfCond = conditionVars.get(0);
        CDD selfCons = constraintVars.get(0);

        boolean ii = othCond.isEqual(selfCond);
        boolean ic = othCond.isEqual(selfCons);
        boolean ci = othCons.isEqual(selfCond);
        boolean cc = othCons.isEqual(selfCons);

        if ((ii && ci) || (ic & cc)) {
            return ReqOverlapping.TwoParts;
        }
        if (ic && ci) {
            return ReqOverlapping.Loop;
        }
        if (ii && cc) {
            return ReqOverlapping.Full;
        }
        if (ii) {
            return ReqOverlapping.Head;
        }
        if (ic || ci) {
            return ReqOverlapping.HeadTail;
        }
        if (cc) {
            return ReqOverlapping.Tail;
        }
        return ReqOverlapping.None;
    }

    public PatternType<?> getReq() {
        return originReq;
    }
}
