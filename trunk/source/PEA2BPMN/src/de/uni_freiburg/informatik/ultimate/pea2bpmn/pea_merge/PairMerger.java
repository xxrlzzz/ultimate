package de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_merge;


import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.ReqDesc;

public class PairMerger {
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
}
