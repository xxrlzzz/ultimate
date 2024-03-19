package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;

import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

/**
 * 顺序合并，我的输出是你的输入
 */
public class SequenceMerge extends IPeaMerger {
    @Override
    public PEAFragment merge(PEAFragment left, PEAFragment right) {
        preMerge(left, right);

        if (mConditionPhase == null || oConditionPhase == null || mConsPhase == null || oConsPhase == null) {
            throw new RuntimeException("not found condition/constraint phase when merge");
        }

        // 变量没对上
        if (!mConsPhase.equalByState(oConditionPhase)) {
            throw new RuntimeException("顺序合并， 变量失配");
        }
//        ReqDesc leftDesc = left.getDesc(), rightDesc = right.getDesc();

        // 直接迁移就可以
        PairMergerUtils.migratePhase(oConditionPhase, mConsPhase, phases, null);

        phases.remove(oConditionPhase);
        inits.remove(oConditionPhase);
        return makeFragment(left.getName() + "-" + right.getName());
    }
}
