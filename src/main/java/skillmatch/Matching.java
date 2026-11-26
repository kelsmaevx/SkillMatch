package skillmatch;

import java.util.*;

interface Matcher {
    MatchResult match(Profile p, Opportunity o);
    List<MatchResult> rank(Profile p, List<Opportunity> all);
}
interface Recommender { List<MatchResult> recommend(Profile p, int limit); }

class SkillGapAnalyzer {
    List<String> matched(Profile p, Opportunity o) {
        Set<String> have = p.skillSet(); List<String> out = new ArrayList<>();
        for (String s : o.getRequiredSkills()) if (have.contains(Profile.norm(s))) out.add(s);
        return out;
    }
    List<String> missing(Profile p, Opportunity o) {
        Set<String> have = p.skillSet(); List<String> out = new ArrayList<>();
        for (String s : o.getRequiredSkills()) if (!have.contains(Profile.norm(s))) out.add(s);
        return out;
    }
}

/** Deterministic weighted score: skills 40, experience 20, education 15, interests 10, location+preferences 15. */
class MatchingEngine implements Matcher {
    private final SkillGapAnalyzer gap = new SkillGapAnalyzer();

    @Override public MatchResult match(Profile p, Opportunity o) {
        List<String> matched = gap.matched(p, o), missing = gap.missing(p, o);
        double skills = o.getRequiredSkills().isEmpty() ? 1 : (double) matched.size() / o.getRequiredSkills().size();
        double reqExp = o.requiredExperience();
        double exp = reqExp <= 0 ? 1 : Math.min(1, p.getExperienceYears() / reqExp);
        int need = Profile.educationLevel(o.getMinEducation()), have = Profile.educationLevel(p.getEducation());
        double edu = have >= need ? 1 : (double) have / need;
        Set<String> tags = new HashSet<>(); for (String t : o.getTags()) tags.add(Profile.norm(t));
        long overlap = p.interestSet().stream().filter(tags::contains).count();
        double interests = p.getInterests().isEmpty() || tags.isEmpty() ? 0.5 : Math.min(1, overlap / (double) Math.min(3, tags.size()));
        boolean locOk = "Any".equalsIgnoreCase(p.getLocation()) || "Remote".equalsIgnoreCase(o.getLocation())
                || p.getLocation().equalsIgnoreCase(o.getLocation());
        boolean typeOk = "Any".equals(p.getPreferredType()) || p.getPreferredType().equals(o.getType());
        double locPref = (locOk ? 10 : 0) + (typeOk ? 5 : 0);

        Map<String, Integer> b = new LinkedHashMap<>();
        b.put("Skills", (int) Math.round(skills * 40)); b.put("Experience", (int) Math.round(exp * 20));
        b.put("Education", (int) Math.round(edu * 15)); b.put("Interests", (int) Math.round(interests * 10));
        b.put("Location & Preferences", (int) Math.round(locPref));
        int score = Math.max(0, Math.min(100, b.values().stream().mapToInt(Integer::intValue).sum()));

        MatchResult r = new MatchResult();
        r.id = o.getId(); r.title = o.getTitle(); r.score = score;
        r.level = CompatibilityLevel.fromScore(score).label();
        r.matched = matched; r.missing = missing; r.breakdown = b;
        r.explanation = String.format("%s is a %s (%d%%). You have %d of %d required skills%s.", o.getTitle(), r.level, score,
            matched.size(), o.getRequiredSkills().size(), missing.isEmpty() ? "" : "; the main gaps are " + String.join(", ", missing.subList(0, Math.min(3, missing.size()))));
        return r;
    }
    @Override public List<MatchResult> rank(Profile p, List<Opportunity> all) {
        List<MatchResult> out = new ArrayList<>();
        for (Opportunity o : all) out.add(match(p, o));
        out.sort(Comparator.comparingInt((MatchResult r) -> -r.score).thenComparing(r -> r.title));
        return out;
    }
}

class TrainingRecommender {
    private static final Map<String, String> CATALOG = new HashMap<>();
    static {
        String[][] c = {
            {"docker", "Containerize a small app and run it with Docker Compose alongside a database."},
            {"spring boot", "Build a REST API with Spring Boot and cover it with JUnit tests."},
            {"aws", "Complete the AWS Cloud Practitioner labs: IAM, EC2, S3, and a managed database."},
            {"azure", "Work through Microsoft Learn's Azure Fundamentals path and deploy a sample web app."},
            {"ci/cd", "Add a GitHub Actions workflow that builds and tests one of your repositories."},
            {"typescript", "Convert a small JavaScript project to TypeScript to practice types and interfaces."},
            {"rest apis", "Design and document a small REST API, then test it with Postman."},
            {"sql", "Practice joins, grouping, and indexes on a sample database such as Sakila."},
            {"git", "Use branches and pull requests on a personal project; practice resolving merge conflicts."},
            {"react", "Build a small single-page app with React that fetches data from an API."},
            {"junit", "Write unit tests for an existing Java project and aim for meaningful coverage."},
            {"linux", "Set up a Linux VM and practice the shell, permissions, and basic services."},
            {"python", "Complete a Python project such as a CLI tool or a data-cleaning script."},
            {"selenium", "Automate a login-and-search flow on a demo website with Selenium."},
            {"kubernetes", "Deploy a containerized app to a local cluster (minikube) and scale it."},
            {"machine learning", "Train and evaluate a simple model on a public dataset with scikit-learn."},
        };
        for (String[] e : c) CATALOG.put(e[0], e[1]);
    }
    List<String> recommend(List<String> missing) { List<String> out = new ArrayList<>(); for (String s : missing) out.add(stepFor(s)); return out; }
    String stepFor(String skill) {
        return CATALOG.getOrDefault(Profile.norm(skill), "Take an introductory course on " + skill + " and build a small project that uses it.");
    }
}

class RecommendationEngine implements Recommender {
    private final Matcher matcher; private final OpportunityRepository repo; private final TrainingRecommender training;
    RecommendationEngine(Matcher m, OpportunityRepository r, TrainingRecommender t) { matcher = m; repo = r; training = t; }
    @Override public List<MatchResult> recommend(Profile p, int limit) {
        List<MatchResult> top = new ArrayList<>(matcher.rank(p, repo.all()).subList(0, Math.min(limit, repo.all().size())));
        for (MatchResult r : top) r.training = training.recommend(r.missing);
        return top;
    }
}

class SkillGapSummarizer {
    private static final int MAX_ITEMS = 5;
    private final TrainingRecommender training;
    SkillGapSummarizer(TrainingRecommender t) { training = t; }
    GapSummary summarize(List<MatchResult> top) {
        Map<String, Integer> count = new LinkedHashMap<>();
        for (MatchResult r : top) for (String s : r.missing) count.merge(s, 1, Integer::sum);
        List<Map.Entry<String, Integer>> e = new ArrayList<>(count.entrySet());
        e.sort((a, b) -> b.getValue() != a.getValue().intValue() ? b.getValue() - a.getValue() : a.getKey().compareTo(b.getKey()));
        GapSummary g = new GapSummary();
        for (Map.Entry<String, Integer> x : e.subList(0, Math.min(MAX_ITEMS, e.size()))) {
            GapItem i = new GapItem(); i.skill = x.getKey(); i.missingIn = x.getValue(); i.outOf = top.size(); i.advice = training.stepFor(x.getKey());
            g.items.add(i);
        }
        g.summary = g.items.isEmpty() ? "You already have the skills listed for your top matches. Keep your projects and resume up to date."
            : "Across your top " + top.size() + " matches, the skills missing most often are " + String.join(", ", g.items.stream().limit(3).map(i -> i.skill).toList())
              + ". Focusing on these first will raise your compatibility the most.";
        return g;
    }
}
