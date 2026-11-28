package skillmatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Profile {
    private String name;
    private List<String> skills;
    private double experienceYears;
    private String education;
    private List<String> interests;
    private String location;
    private String preferredType;

    public Profile() {
        skills = new ArrayList<>();
        interests = new ArrayList<>();
        location = "Any";
        preferredType = "Any";
        education = "";
    }

    public Profile(String name, List<String> skills, double experienceYears,
                   String education, List<String> interests, String location,
                   String preferredType) {
        this.name = name;
        this.skills = skills == null ? new ArrayList<String>() : new ArrayList<>(skills);
        this.experienceYears = experienceYears;
        this.education = education == null ? "" : education;
        this.interests = interests == null ? new ArrayList<String>() : new ArrayList<>(interests);
        this.location = location == null ? "Any" : location;
        this.preferredType = preferredType == null ? "Any" : preferredType;
    }

    public String getName() {
        return name;
    }

    public List<String> getSkills() {
        return new ArrayList<>(skills);
    }

    public double getExperienceYears() {
        return experienceYears;
    }

    public String getEducation() {
        return education;
    }

    public List<String> getInterests() {
        return new ArrayList<>(interests);
    }

    public String getLocation() {
        return location;
    }

    public String getPreferredType() {
        return preferredType;
    }

    public Set<String> skillSet() {
        Set<String> result = new HashSet<>();
        for (String skill : skills) {
            if (skill != null && !norm(skill).isEmpty()) {
                result.add(norm(skill));
            }
        }
        return result;
    }

    public Set<String> interestSet() {
        Set<String> result = new HashSet<>();
        for (String interest : interests) {
            if (interest != null && !norm(interest).isEmpty()) {
                result.add(norm(interest));
            }
        }
        return result;
    }

    public static String norm(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
