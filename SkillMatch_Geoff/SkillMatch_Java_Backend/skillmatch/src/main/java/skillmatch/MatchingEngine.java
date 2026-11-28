package skillmatch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchingEngine implements Matcher {
    private static final int SKILL_WEIGHT = 40;
    private static final int EXPERIENCE_WEIGHT = 20;
    private static final int EDUCATION_WEIGHT = 15;
    private static final int INTEREST_WEIGHT = 10;
    private static final int PREFERENCE_WEIGHT = 15;

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
        if (!o.getRequiredSkills().isEmpty()) {
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
        double educationScore = 0;

        if (requiredEducation == 0) {
            educationScore = 1;
        } else if (userEducation > 0) {
            educationScore = (double) userEducation / requiredEducation;
            if (educationScore > 1) {
                educationScore = 1;
            }
        }

        
        // Calculate the interests score

        double interestScore = 0.5;
        Set<String> userInterests = p.interestSet();
        Set<String> opportunityTags = new java.util.HashSet<>();
        for (String tag : o.getTags()) {
            if (tag != null && !Profile.norm(tag).isEmpty()) {
                opportunityTags.add(Profile.norm(tag));
            }
        }

        if (!userInterests.isEmpty() && !opportunityTags.isEmpty()) {
            int matches = 0;

            for (String tag : opportunityTags) {
                if (userInterests.contains(tag)) {
                    matches++;
                }
            }

            int total = Math.min(3, opportunityTags.size());
            interestScore = (double) matches / total;

            if (interestScore > 1) {
                interestScore = 1;
            }
        }


         // Check the user's location and preferred job type
         
        String userLocation = Profile.norm(p.getLocation());
        String opportunityLocation = Profile.norm(o.getLocation());

        boolean locationMatch = userLocation.isEmpty()
                || userLocation.equals("any")
                || opportunityLocation.equals("remote")
                || userLocation.equals(opportunityLocation);

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

        Map<String, Integer> breakdown = new LinkedHashMap<>();
        breakdown.put("Skills", weightedPoints(skillsScore, SKILL_WEIGHT));
        breakdown.put("Experience", weightedPoints(experienceScore, EXPERIENCE_WEIGHT));
        breakdown.put("Education", weightedPoints(educationScore, EDUCATION_WEIGHT));
        breakdown.put("Interests", weightedPoints(interestScore, INTEREST_WEIGHT));
        breakdown.put("Location and Preferences", weightedPoints(preferenceScore, PREFERENCE_WEIGHT));

        int finalScore = 0;
        for (int points : breakdown.values()) {
            finalScore += points;
        }

        MatchResult result = new MatchResult();
        result.id = o.getId();
        result.title = o.getTitle();
        result.matched = matched;
        result.missing = missing;
        result.breakdown = breakdown;
        result.score = finalScore;
        result.level = CompatibilityLevel.fromScore(finalScore).label();
        result.gapSummary = new SkillGapSummarizer().summarize(p, o);

        return result;
    }


    // Changes the education level into a number for comparison

    private int weightedPoints(double criterionScore, int weight) {
        return (int) Math.round(criterionScore * weight);
    }

    private int educationValue(String education) {
        String value = Profile.norm(education).replaceAll("[^a-z]", "");

        if (value.isEmpty()) {
            return 0;
        } else if (value.contains("doctor") || value.contains("phd")) {
            return 5;
        } else if (value.contains("master") || value.contains("graduate")) {
            return 4;
        } else if (value.contains("bachelor") || value.equals("collegegraduate")) {
            return 3;
        } else if (value.contains("undergraduate") || value.contains("associate")
                || value.equals("college")) {
            return 2;
        } else if (value.contains("highschool") || value.contains("seniorhigh")
                || value.contains("secondary")) {
            return 1;
        }

        return 0;
    }
}
