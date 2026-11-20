package skillmatch;

import java.util.*;
import java.util.stream.Collectors;


public interface Matcher {
    MatchResult match(Profile p, Opportunity o);


    default List<MatchResult> rank(Profile p, List<Opportunity> all) {
        return all.stream().map(o -> match(p, o))
            .sorted((a, b) -> Integer.compare(b.score, a.score))
            .collect(Collectors.toList());
    }
}
