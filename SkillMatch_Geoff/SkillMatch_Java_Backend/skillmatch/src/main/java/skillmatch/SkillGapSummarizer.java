package skillmatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SkillGapSummarizer {
    public GapSummary summarize(Profile profile, Opportunity opportunity) {
        List<GapItem> items = new ArrayList<>();
        Set<String> userSkills = profile.skillSet();
        int matchedCount = 0;
        int missingCount = 0;

        for (String skill : opportunity.getRequiredSkills()) {
            boolean matched = userSkills.contains(Profile.norm(skill));
            items.add(new GapItem(skill, matched));

            if (matched) {
                matchedCount++;
            } else {
                missingCount++;
            }
        }

        return new GapSummary(items, matchedCount, missingCount);
    }
}
