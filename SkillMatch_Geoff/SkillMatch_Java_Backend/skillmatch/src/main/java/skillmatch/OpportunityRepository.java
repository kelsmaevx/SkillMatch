package skillmatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpportunityRepository {
    private final List<Opportunity> opportunities;

    public OpportunityRepository() {
        opportunities = new ArrayList<>();

        opportunities.add(new Opportunity(
                "JOB001", "Junior Java Developer", "Sample Technology Company",
                "Full-time", Arrays.asList("Java", "OOP", "SQL", "Git"),
                0, "Bachelor's Degree",
                Arrays.asList("software development", "backend", "programming"),
                "Any"));

        opportunities.add(new Opportunity(
                "JOB002", "Computer Programmer", "Sample Software Company",
                "Full-time", Arrays.asList("C++", "Java", "Python", "SQL"),
                1, "Undergraduate",
                Arrays.asList("programming", "software development", "coding"),
                "Any"));

        opportunities.add(new Opportunity(
                "INT001", "Web Developer Intern", "Sample Digital Studio",
                "Internship", Arrays.asList("HTML", "CSS", "JavaScript", "Git"),
                0, "Undergraduate",
                Arrays.asList("web development", "frontend", "design"),
                "Remote"));

        opportunities.add(new Opportunity(
                "JOB003", "Database Developer", "Sample Data Solutions",
                "Full-time", Arrays.asList("SQL", "MySQL", "Database Design", "Python"),
                1, "Bachelor's Degree",
                Arrays.asList("database", "data", "backend"),
                "Any"));

        opportunities.add(new Opportunity(
                "INT002", "Software Developer Intern", "Sample IT Services",
                "Internship", Arrays.asList("Java", "Python", "OOP", "Debugging"),
                0, "Undergraduate",
                Arrays.asList("software development", "programming", "internship"),
                "Remote"));
    }

    public List<Opportunity> all() {
        return new ArrayList<>(opportunities);
    }
}
