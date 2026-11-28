package skillmatch;

public class GapItem {
    private String skill;
    private boolean matched;

    public GapItem(String skill, boolean matched) {
        this.skill = skill;
        this.matched = matched;
    }

    public String getSkill() {
        return skill;
    }

    public boolean isMatched() {
        return matched;
    }
}
