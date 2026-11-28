package skillmatch;

import java.util.ArrayList;
import java.util.List;

public class Opportunity {
    private String id;
    private String title;
    private String company;
    private String type;
    private List<String> requiredSkills;
    private double requiredExperience;
    private String minEducation;
    private List<String> tags;
    private String location;

    public Opportunity(String id, String title, String company, String type,
                       List<String> requiredSkills, double requiredExperience,
                       String minEducation, List<String> tags, String location) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.type = type;
        this.requiredSkills = requiredSkills == null ? new ArrayList<String>() : new ArrayList<>(requiredSkills);
        this.requiredExperience = requiredExperience;
        this.minEducation = minEducation == null ? "" : minEducation;
        this.tags = tags == null ? new ArrayList<String>() : new ArrayList<>(tags);
        this.location = location == null ? "Remote" : location;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getType() {
        return type;
    }

    public List<String> getRequiredSkills() {
        return new ArrayList<>(requiredSkills);
    }

    public double requiredExperience() {
        return requiredExperience;
    }

    public String getMinEducation() {
        return minEducation;
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public String getLocation() {
        return location;
    }
}
