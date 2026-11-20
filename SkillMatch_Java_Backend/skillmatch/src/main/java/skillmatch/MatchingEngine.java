package skillmatch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchingEngine implements Matcher {

    private final double SKILL_WEIGHT = 0.40;
    private final double EXPERIENCE_WEIGHT = 0.20;
    private final double EDUCATION_WEIGHT = 0.15;
    private final double INTEREST_WEIGHT = 0.10;
    private final double PREFERENCE_WEIGHT = 0.15;

    private final SkillGapAnalyzer gap;

    public MatchingEngine() {
        gap = new SkillGapAnalyzer();
    }

    public MatchingEngine(SkillGapAnalyzer gap) {
        this.gap = gap;
    }

    @Override
    public MatchResult match(Profile p, Opportunity o) {
        List<String> matched = gap.matched(p, o);
        List<String> missing = gap.missing(p, o);

        // Calculate the skills score
        double skillsScore = 1;
        if (o.getRequiredSkills().size() > 0) {
            skillsScore = (double) matched.size() / o.getRequiredSkills().size();
        }

        // Calculate the experience score
        double experienceScore = 1;
        double requiredExperience = o.requiredExperience();

        if (requiredExperience > 0) {
            experienceScore = p.getExperienceYears() / requiredExperience;
            if (experienceScore > 1) {
                experienceScore = 1;
            }
        }

        // Calculate the education score
        int userEducation = educationValue(p.getEducation());
        int requiredEducation = educationValue(o.getMinEducation());
        double educationScore = (double) userEducation / requiredEducation;

        if (educationScore > 1) {
            educationScore = 1;
        }

        // Calculate the interests score
        double interestScore = 0.5;
        Set<String> userInterests = p.interestSet();

        if (!userInterests.isEmpty()) {
            int matches = 0;

            for (String tag : o.getTags()) {
                if (userInterests.contains(Profile.norm(tag))) {
                    matches++;
                }
            }

            int total = userInterests.size();
            if (total > 2) {
                total = 2;
            }

            interestScore = (double) matches / total;
            if (interestScore > 1) {
                interestScore = 1;
            }
        }

        // Check the user's location and preferred job type
        boolean locationMatch = Profile.norm(p.getLocation()).isEmpty()
                || Profile.norm(o.getLocation()).equals("remote")
                || Profile.norm(p.getLocation()).equals(Profile.norm(o.getLocation()));

        boolean typeMatch = Profile.norm(p.getPreferredType()).equals("any")
                || Profile.norm(p.getPreferredType()).equals(Profile.norm(o.getType()));

        double preferenceScore = 0;
        if (locationMatch) {
            preferenceScore += 0.5;
        }
        if (typeMatch) {
            preferenceScore += 0.5;
        }

        // Get the final score
        double finalScore = (skillsScore * SKILL_WEIGHT)
                + (experienceScore * EXPERIENCE_WEIGHT)
                + (educationScore * EDUCATION_WEIGHT)
                + (interestScore * INTEREST_WEIGHT)
                + (preferenceScore * PREFERENCE_WEIGHT);

        MatchResult result = new MatchResult();
        result.id = o.getId();
        result.title = o.getTitle();
        result.company = o.getCompany();
        result.type = o.getType();
        result.location = o.getLocation();
        result.matched = matched;
        result.missing = missing;
        result.score = (int) Math.round(finalScore * 100);
        result.level = CompatibilityLevel.fromScore(result.score).label();

        Map<String, Integer> breakdown = new LinkedHashMap<>();
        breakdown.put("Skills", toPercent(skillsScore));
        breakdown.put("Experience", toPercent(experienceScore));
        breakdown.put("Education", toPercent(educationScore));
        breakdown.put("Interests", toPercent(interestScore));
        breakdown.put("Location/Preferences", toPercent(preferenceScore));
        result.breakdown = breakdown;

        return result;
    }

    // Changes the education level into a number for comparison
    private int educationValue(String education) {
        String value = Profile.norm(education);

        if (value.equals("highschool")) {
            return 1;
        } else if (value.equals("undergraduate")) {
            return 2;
        } else if (value.equals("bachelor")) {
            return 3;
        } else if (value.equals("graduate")) {
            return 4;
        }

        return 2;
    }

    private int toPercent(double value) {
        return (int) Math.round(value * 100);
    }
}
