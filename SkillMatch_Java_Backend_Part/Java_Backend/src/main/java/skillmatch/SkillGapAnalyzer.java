package skillmatch;

import java.util.*;
import java.util.stream.Collectors;

public class SkillGapAnalyzer {
    public List<String> matched(Profile p, Opportunity o) {
        Set<String> have = p.skillSet();
        return o.getRequiredSkills().stream().filter(s -> have.contains(Profile.norm(s))).collect(Collectors.toList());
    }
    public List<String> missing(Profile p, Opportunity o) {
        Set<String> have = p.skillSet();
        return o.getRequiredSkills().stream().filter(s -> !have.contains(Profile.norm(s))).collect(Collectors.toList());
    }
}
