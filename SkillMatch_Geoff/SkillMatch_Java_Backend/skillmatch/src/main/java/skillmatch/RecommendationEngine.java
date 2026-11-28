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
        StringBuilder message = new StringBuilder();

        message.append("Your profile matches ")
                .append(result.gapSummary.getMatchedCount())
                .append(" out of ")
                .append(result.gapSummary.getTotalCount())
                .append(" required skills. ");

        message.append("Your overall compatibility score is ")
                .append(result.score)
                .append("% (")
                .append(result.level)
                .append("). ");

        message.append("Weighted score points: ");
        boolean first = true;

        for (String criterion : result.breakdown.keySet()) {
            if (!first) {
                message.append(", ");
            }
            message.append(criterion).append(": ").append(result.breakdown.get(criterion));
            first = false;
        }

        if (!result.matched.isEmpty()) {
            message.append(". Skills matched: ").append(String.join(", ", result.matched));
        }

        if (!result.missing.isEmpty()) {
            message.append(". Skills to improve: ").append(String.join(", ", result.missing));
        }

        return message.toString();
    }
}
