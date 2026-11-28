package skillmatch;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Profile profile = new Profile(
                "Sample User",
                Arrays.asList("Java", "OOP", "SQL", "Git"),
                0.5,
                "Undergraduate",
                Arrays.asList("programming", "software development"),
                "Any",
                "Any"
        );

        OpportunityRepository repository = new OpportunityRepository();
        MatchingEngine matcher = new MatchingEngine();
        TrainingRecommender training = new TrainingRecommender();

        RecommendationEngine engine = new RecommendationEngine(matcher, repository, training);
        List<MatchResult> results = engine.recommend(profile, 5);

        for (MatchResult result : results) {
            System.out.println(result.title);
            System.out.println("Score: " + result.score + "% - " + result.level);
            System.out.println(result.explanation);

            if (!result.training.isEmpty()) {
                System.out.println("Suggested learning steps:");
                for (String step : result.training) {
                    System.out.println("- " + step);
                }
            }

            System.out.println();
        }
    }
}
