package skillmatch;

import java.util.*;


public class MatchResult {
    public String id, title, company, type, location;
    public int score;
    public String level;                 
    public List<String> matched, missing;
    public Map<String, Integer> breakdown;
    public String explanation;
    public List<String> training;
}
