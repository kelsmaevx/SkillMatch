package skillmatch;

import java.util.*;
import java.util.regex.Pattern;

abstract class Opportunity {
    protected String id, title, company, location, description, minEducation;
    protected double minExperience;
    protected List<String> requiredSkills, tags;

    Opportunity(String id, String title, String company, String location, String description,
                String minEducation, double minExperience, List<String> requiredSkills, List<String> tags) {
        this.id = id; this.title = title; this.company = company; this.location = location;
        this.description = description; this.minEducation = Profile.normEducation(minEducation);
        this.minExperience = minExperience;
        this.requiredSkills = List.copyOf(requiredSkills); this.tags = List.copyOf(tags);
    }
    public abstract String getType();
    public double requiredExperience() { return minExperience; }
    public void validate() {
        for (String f : new String[]{id, title, company, location})
            if (f == null || f.isBlank()) throw new IllegalArgumentException("Opportunity " + id + " is missing a required field");
        if (requiredSkills.isEmpty()) throw new IllegalArgumentException(id + " has no required skills");
        if (!TechCatalog.isTechOpportunity(this)) throw new IllegalArgumentException(id + " is not tech-related");
    }
    public String getId() { return id; } public String getTitle() { return title; }
    public String getCompany() { return company; } public String getLocation() { return location; }
    public String getMinEducation() { return minEducation; }
    public List<String> getRequiredSkills() { return requiredSkills; } public List<String> getTags() { return tags; }
}

class Job extends Opportunity {
    private final String salary;
    Job(String id, String title, String company, String location, String description, String minEducation,
        double minExperience, List<String> skills, List<String> tags, String salary) {
        super(id, title, company, location, description, minEducation, minExperience, skills, tags);
        this.salary = salary == null ? "" : salary;
    }
    @Override public String getType() { return "Job"; }
}

class Internship extends Opportunity {
    private final int durationWeeks;
    Internship(String id, String title, String company, String location, String description, String minEducation,
               double minExperience, List<String> skills, List<String> tags, int durationWeeks) {
        super(id, title, company, location, description, minEducation, minExperience, skills, tags);
        this.durationWeeks = durationWeeks;
    }
    @Override public String getType() { return "Internship"; }
    @Override public double requiredExperience() { return Math.min(minExperience, 0.5); }
}

class Profile {
    static final List<String> EDUCATION = List.of("High School", "Vocational", "Bachelor's", "Master's", "Doctorate");
    private final List<String> skills, interests;
    private final String education, location, preferredType;
    private final double experienceYears;

    Profile(List<String> skills, List<String> interests, String education, String location,
            String preferredType, double experienceYears) {
        this.skills = skills == null ? List.of() : List.copyOf(skills);
        this.interests = interests == null ? List.of() : List.copyOf(interests);
        this.education = normEducation(education);
        this.location = location == null || location.isBlank() ? "Any" : location.trim();
        String t = preferredType == null ? "" : preferredType.trim().toLowerCase();
        this.preferredType = t.startsWith("intern") ? "Internship" : t.startsWith("job") ? "Job" : "Any";
        this.experienceYears = Double.isNaN(experienceYears) ? 0 : Math.max(0, Math.min(40, experienceYears));
    }
    static String normEducation(String s) {
        String t = s == null ? "" : s.toLowerCase();
        if (t.matches(".*(doctor|ph\\.?d).*")) return "Doctorate";
        if (t.matches(".*(master|m\\.?s\\.?c?\\b|mba).*")) return "Master's";
        if (t.matches(".*(bachelor|b\\.?s\\.?c?\\b|college|undergrad|bs ).*")) return "Bachelor's";
        if (t.matches(".*(vocational|diploma|associate|tesda|technical).*")) return "Vocational";
        return "High School";
    }
    static int educationLevel(String e) { return EDUCATION.indexOf(normEducation(e)) + 1; }
    Profile withLocation(String loc) { return new Profile(skills, interests, education, loc, preferredType, experienceYears); }
    Set<String> skillSet() { return lower(skills); }
    Set<String> interestSet() { return lower(interests); }
    static String norm(String s) { return s == null ? "" : s.trim().toLowerCase(); }
    private static Set<String> lower(List<String> l) { Set<String> s = new LinkedHashSet<>(); for (String x : l) s.add(norm(x)); return s; }
    List<String> getSkills() { return skills; } List<String> getInterests() { return interests; }
    String getEducation() { return education; } String getLocation() { return location; }
    String getPreferredType() { return preferredType; } double getExperienceYears() { return experienceYears; }
}

enum CompatibilityLevel {
    STRONG("Strong Match"), GOOD("Good Match"), FAIR("Fair Match"), LOW("Low Match");
    private final String label;
    CompatibilityLevel(String l) { label = l; }
    String label() { return label; }
    static CompatibilityLevel fromScore(int s) { return s >= 80 ? STRONG : s >= 60 ? GOOD : s >= 40 ? FAIR : LOW; }
}

class MatchResult {
    public String id, title, level, explanation;
    public int score;
    public List<String> matched, missing, training = new ArrayList<>();
    public Map<String, Integer> breakdown;
}

class GapItem { public String skill, advice; public int missingIn, outOf; }
class GapSummary { public List<GapItem> items = new ArrayList<>(); public String summary = ""; }
class ResumeResult { public Profile profile; public boolean usedAi, techRelated; }
class AnalysisResult {
    public Profile profile; public boolean usedAi, techRelated;
    public List<MatchResult> results = new ArrayList<>(); public GapSummary gaps;
}

class TechCatalog {
    private static final List<String> SKILLS = List.of(
        "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "C", "PHP", "Kotlin", "Swift", "Golang", "Rust", "Ruby", "SQL",
        "HTML", "CSS", "React", "Angular", "Vue", "Node.js", "Express", "Spring Boot", "Spring", "Django", "Flask", "Laravel",
        ".NET", "Android", "Flutter", "MySQL", "PostgreSQL", "MongoDB", "SQLite", "Oracle", "Redis", "Firebase",
        "Git", "GitHub", "Docker", "Kubernetes", "Linux", "AWS", "Azure", "Google Cloud", "CI/CD", "GitHub Actions", "Jenkins", "Terraform",
        "REST APIs", "GraphQL", "JUnit", "Selenium", "Cypress", "Postman", "Jira", "Agile", "Scrum", "OOP", "Data Structures", "Algorithms",
        "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch", "Pandas", "NumPy", "Power BI", "Tableau", "Excel", "Data Analysis",
        "Networking", "Cybersecurity", "Penetration Testing", "Troubleshooting", "Help Desk", "Windows Server", "Figma", "UI/UX", "Bootstrap", "Tailwind CSS");
    private static final Pattern TECH_TITLE = Pattern.compile(
        "(?i)developer|engineer|programmer|software|web\\b|data\\b|devops|\\bqa\\b|tester|analyst|security|cloud|network|help desk|it support|administrator|full.?stack|front.?end|back.?end|mobile|machine learning|\\bai\\b|ui/ux");
    static List<String> skills() { return SKILLS; }
    static boolean isTechSkill(String s) { String n = Profile.norm(s); return SKILLS.stream().anyMatch(k -> k.toLowerCase().equals(n)); }
    static boolean hasTechSkill(Collection<String> skills) { return skills.stream().anyMatch(TechCatalog::isTechSkill); }
    static boolean isTechOpportunity(Opportunity o) { return TECH_TITLE.matcher(o.getTitle()).find() || hasTechSkill(o.getRequiredSkills()); }
}
