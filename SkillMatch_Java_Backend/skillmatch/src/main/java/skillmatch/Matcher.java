package skillmatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface Matcher {
    MatchResult match(Profile p, Opportunity o);

    // Sorts the opportunities from highest score to lowest score
    default List<MatchResult> rank(Profile p, List<Opportunity> all) {
        List<MatchResult> results = new ArrayList<>();

        for (Opportunity opportunity : all) {
            results.add(match(p, opportunity));
        }

        Collections.sort(results, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                return Integer.compare(b.score, a.score);
            }
        });

        return results;
    }
}
