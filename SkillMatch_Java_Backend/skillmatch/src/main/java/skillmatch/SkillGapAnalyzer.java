package skillmatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SkillGapAnalyzer {

    // Gets the skills that the user already has
    public List<String> matched(Profile p, Opportunity o) {
        List<String> result = new ArrayList<>();
        Set<String> userSkills = p.skillSet();

        for (String skill : o.getRequiredSkills()) {
            if (userSkills.contains(Profile.norm(skill))) {
                result.add(skill);
            }
        }

        return result;
    }

    // Gets the skills that the user still needs
    public List<String> missing(Profile p, Opportunity o) {
        List<String> result = new ArrayList<>();
        Set<String> userSkills = p.skillSet();

        for (String skill : o.getRequiredSkills()) {
            if (!userSkills.contains(Profile.norm(skill))) {
                result.add(skill);
            }
        }

        return result;
    }
}
