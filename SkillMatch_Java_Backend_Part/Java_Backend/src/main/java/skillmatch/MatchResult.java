package skillmatch;

import java.util.*;

/** Output of the matching engine (public fields so Gson can send it as JSON). */
public class MatchResult {
    public String id, title, company, type, location;
    public int score;
    public String level;                 // "Strong Match", "Good Match", ... (see CompatibilityLevel)
    public List<String> matched, missing;
    public Map<String, Integer> breakdown;
    public String explanation;
    public List<String> training;
}
