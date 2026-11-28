# SkillMatch - Java Backend

This folder contains the Java matching and recommendation backend.

## Included classes

- `Main.java` - runs a small example of the matching system.
- `Profile.java` - stores the applicant's information.
- `Opportunity.java` - stores one job or internship opportunity.
- `OpportunityRepository.java` - holds the sample opportunities used by this example.
- `CompatibilityLevel.java` - converts a score into a match label.
- `Matcher.java` - defines matching and ranking behavior.
- `MatchingEngine.java` - calculates the weighted compatibility score.
- `MatchResult.java` - stores the result returned for an opportunity.
- `SkillGapAnalyzer.java` - finds matched and missing skills.
- `GapItem.java` - stores one required skill and whether it matches.
- `GapSummary.java` - stores the total matched and missing skill counts.
- `SkillGapSummarizer.java` - creates a summary of the skill gaps.
- `Recommender.java` - defines recommendation behavior.
- `RecommendationEngine.java` - ranks opportunities and prepares explanations.
- `TrainingRecommender.java` - provides a learning suggestion for every missing skill.

## Matching weights

- Skills: 40 points
- Experience: 20 points
- Education: 15 points
- Interests: 10 points
- Location and Preferences: 15 points

The breakdown displays weighted points, not separate percentages. The five breakdown values add up to the overall score out of 100.

For the interests score, the number of matching interests is divided by the smaller of three or the number of distinct tags attached to the opportunity.

A profile location of `Any` receives location credit. A remote opportunity also receives location credit.

Education labels are normalized to support common forms such as `Senior High School`, `Undergraduate`, `Bachelor's Degree`, `Master's Degree`, and `Doctorate`. Unknown education labels are not automatically treated as a degree level. If the opportunity does not specify a minimum education level, the education criterion receives full credit.

## Sample opportunity data

`OpportunityRepository` contains sample records for testing. The company names are placeholders. These records are not live job vacancies and should not be described as actual listings from employers.

The sample skills are example requirements informed by occupational skills references. Replace or update them with the exact job requirements and education levels approved in the project proposal.


