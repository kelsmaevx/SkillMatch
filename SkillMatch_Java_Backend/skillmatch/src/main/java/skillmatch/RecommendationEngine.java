package skillmatch;

import java.util.ArrayList;
import java.util.List;

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
        List<MatchResult> ranked = matcher.rank(p, repo.all());
        List<MatchResult> results = new ArrayList<>();

        // Only get the number of results requested
        
        for (int i = 0; i < ranked.size() && i < limit; i++) {
            MatchResult result = ranked.get(i);
            result.explanation = describe(result);
            result.training = training.recommend(result.missing);
            results.add(result);
        }

        return results;
    }

    // Makes a short explanation for the match
    
    static String describe(MatchResult result) {
        int totalSkills = result.matched.size() + result.missing.size();

        String message = "You match " + result.matched.size()
                + " of " + totalSkills + " required skills";

        if (result.matched.isEmpty()) {
            message += ".";
        } else {
            message += " (" + String.join(", ", result.matched) + ").";
        }

        if (!result.missing.isEmpty()) {
            message += " To improve your chances, work on: "
                    + String.join(", ", result.missing) + ".";
        }

        return message;
    }
}
