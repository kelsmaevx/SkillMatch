package skillmatch;

import java.util.List;

/** Interface: produces the final recommendations shown to the user. */
public interface Recommender {
    List<MatchResult> recommend(Profile p, int limit);
}
