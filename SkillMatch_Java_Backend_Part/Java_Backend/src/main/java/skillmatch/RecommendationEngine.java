package skillmatch;

import java.util.*;
import java.util.stream.Collectors;

//Recommendation engine: ranks opportunities with a Matcher, then adds an explanation

public class RecommendationEngine implements Recommender {
    private final Matcher matcher;
    private final OpportunityRepository repo;
    private final TrainingRecommender training;

    public RecommendationEngine(Matcher matcher, OpportunityRepository repo, TrainingRecommender training) {
        this.matcher = matcher;
        this.repo = repo;
        this.training = training;
    }

    @Override
    public List<MatchResult> recommend(Profile p, int limit) {
        List<MatchResult> top = matcher.rank(p, repo.all()).stream().limit(limit).collect(Collectors.toList());
        for (MatchResult r : top) {
            r.explanation = describe(r);
            r.training = training.recommend(r.missing);
        }
        return top;
    }

    static String describe(MatchResult r) {
        int total = r.matched.size() + r.missing.size();
        String s = "You match " + r.matched.size() + " of " + total + " required skills"
            + (r.matched.isEmpty() ? "." : " (" + String.join(", ", r.matched) + ").");
        if (!r.missing.isEmpty()) s += " To improve your chances, work on: " + String.join(", ", r.missing) + ".";
        return s;
    }
}
