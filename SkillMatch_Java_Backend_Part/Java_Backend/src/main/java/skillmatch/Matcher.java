package skillmatch;

import java.util.*;
import java.util.stream.Collectors;

/** Interface: anything that can score an opportunity against a profile. */
public interface Matcher {
    MatchResult match(Profile p, Opportunity o);

    /** Default method: every Matcher can rank a list of opportunities, best score first. */
    default List<MatchResult> rank(Profile p, List<Opportunity> all) {
        return all.stream().map(o -> match(p, o))
            .sorted((a, b) -> Integer.compare(b.score, a.score))
            .collect(Collectors.toList());
    }
}
