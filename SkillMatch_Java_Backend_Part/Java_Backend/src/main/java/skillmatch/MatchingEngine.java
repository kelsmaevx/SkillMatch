package skillmatch;

import java.util.*;
import java.util.stream.Collectors;

// Weighted scoring: skills 40, experience 20, education 15, interests 10, location/preferences 15.
public class MatchingEngine implements Matcher {
    private static final double W_SKILL = 0.40, W_EXP = 0.20, W_EDU = 0.15, W_INT = 0.10, W_PREF = 0.15;
    private static final Map<String, Integer> EDU =
        Map.of("highschool", 1, "undergraduate", 2, "bachelor", 3, "graduate", 4);
    private final SkillGapAnalyzer gap;

    public MatchingEngine() { this(new SkillGapAnalyzer()); }
    public MatchingEngine(SkillGapAnalyzer gap) { this.gap = gap; }

    @Override
    public MatchResult match(Profile p, Opportunity o) {
        List<String> matched = gap.matched(p, o), missing = gap.missing(p, o);

        double skills = o.getRequiredSkills().isEmpty() ? 1 : (double) matched.size() / o.getRequiredSkills().size();

        double need = o.requiredExperience();
        double exp = need <= 0 ? 1 : Math.min(1, p.getExperienceYears() / need);

        int have = EDU.getOrDefault(Profile.norm(p.getEducation()), 2);
        int req = EDU.getOrDefault(Profile.norm(o.getMinEducation()), 1);
        double edu = have >= req ? 1 : (double) have / req;

        Set<String> interests = p.interestSet();
        double interest = 0.5;
        if (!interests.isEmpty()) {
            Set<String> tags = o.getTags().stream().map(Profile::norm).collect(Collectors.toSet());
            long hits = interests.stream().filter(tags::contains).count();
            interest = Math.min(1.0, hits / (double) Math.min(interests.size(), 2));
        }

        boolean locOk = Profile.norm(p.getLocation()).isEmpty()
            || Profile.norm(o.getLocation()).equals("remote")
            || Profile.norm(o.getLocation()).equals(Profile.norm(p.getLocation()));
        boolean typeOk = Profile.norm(p.getPreferredType()).equals("any")
            || Profile.norm(p.getPreferredType()).equals(Profile.norm(o.getType()));
        double pref = (locOk ? 0.5 : 0) + (typeOk ? 0.5 : 0);

        MatchResult r = new MatchResult();
        r.id = o.getId(); r.title = o.getTitle(); r.company = o.getCompany();
        r.type = o.getType(); r.location = o.getLocation();
        r.matched = matched; r.missing = missing;
        r.score = (int) Math.round(100 * (W_SKILL * skills + W_EXP * exp + W_EDU * edu + W_INT * interest + W_PREF * pref));
        r.level = CompatibilityLevel.fromScore(r.score).label();
        r.breakdown = new LinkedHashMap<>();
        r.breakdown.put("Skills", pct(skills));
        r.breakdown.put("Experience", pct(exp));
        r.breakdown.put("Education", pct(edu));
        r.breakdown.put("Interests", pct(interest));
        r.breakdown.put("Location/Preferences", pct(pref));
        return r;
    }

    private static int pct(double d) { return (int) Math.round(d * 100); }
}
