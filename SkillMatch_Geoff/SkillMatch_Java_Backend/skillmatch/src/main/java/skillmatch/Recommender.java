package skillmatch;

import java.util.List;

public interface Recommender {
    List<MatchResult> recommend(Profile p, int limit);
}
