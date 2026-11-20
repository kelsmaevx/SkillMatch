# SkillMatch - Java Backend Part

These are the Java classes for the matching and recommendation part of SkillMatch.

## Included classes

- `Matcher.java` - matches a profile with an opportunity and sorts the results.
- `MatchingEngine.java` - calculates the matching score.
- `MatchResult.java` - holds the result details.
- `SkillGapAnalyzer.java` - checks which skills match and which are missing.
- `Recommender.java` - interface for the recommendation part.
- `RecommendationEngine.java` - gets the best matches and adds explanations.
- `TrainingRecommender.java` - gives suggestions for skills that are missing.

## Required project classes

These files work with the other SkillMatch classes:

- `Profile`
- `Opportunity`
- `CompatibilityLevel`
- `OpportunityRepository`


## Matching score

The score uses the weights from the project:

- Skills - 40%
- Experience - 20%
- Education - 15%
- Interests - 10%
- Location and preferences - 15%

