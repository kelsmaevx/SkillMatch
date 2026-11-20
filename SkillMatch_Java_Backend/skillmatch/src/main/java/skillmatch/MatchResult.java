package skillmatch;

import java.util.List;
import java.util.Map;

public class MatchResult {
    public String id;
    public String title;
    public String company;
    public String type;
    public String location;
    public int score;
    public String level;
    public List<String> matched;
    public List<String> missing;
    public Map<String, Integer> breakdown;
    public String explanation;
    public List<String> training;
}
