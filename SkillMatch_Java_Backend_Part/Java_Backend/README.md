# SkillMatch - Java Backend Part

This folder contains my assigned Java Backend classes for the matching and recommendation part of SkillMatch.

## Included Java classes

- `Matcher.java` - interface used for matching a student profile with an opportunity.
- `MatchingEngine.java` - computes the compatibility score using the project's scoring weights.
- `MatchResult.java` - holds the result details, such as the score, matched skills, and missing skills.
- `SkillGapAnalyzer.java` - checks which required skills are already in the profile and which are missing.
- `Recommender.java` - interface for the recommendation process.
- `RecommendationEngine.java` - ranks opportunities and prepares the top recommendations.
- `TrainingRecommender.java` - gives simple learning suggestions for skills that are still missing.

## How the matching score is computed

The score is based on the weights used in the project:

- Skills - 40%
- Experience - 20%
- Education - 15%
- Interests - 10%
- Location and preferences - 15%

The Java code calculates the score. The recommendation engine then adds an explanation and suggests skill needed to learn.

## Needed classes from the main project

- `Profile.java`
- `Opportunity.java`
- `OpportunityRepository.java`
- `CompatibilityLevel.java`


