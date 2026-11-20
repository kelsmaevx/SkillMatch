package skillmatch;

import java.util.*;
import java.util.stream.Collectors;

/** Training recommendations module: turns missing skills into concrete learning steps (rule-based, no AI needed). */
public class TrainingRecommender {
    static final int MAX_STEPS = 3;

    private static final Map<String, String> CATALOG = Map.ofEntries(
        Map.entry("java", "Finish a Java OOP course and build a console app such as a library system."),
        Map.entry("oop", "Practice the four OOP pillars by modelling a small system (classes, inheritance, interfaces)."),
        Map.entry("sql", "Learn SELECT, JOIN and GROUP BY, then design a small MySQL or SQLite database."),
        Map.entry("git", "Create a GitHub account and practice commit, branch, merge and pull request on one of your projects."),
        Map.entry("spring", "Follow the Spring Boot getting-started guide and build a simple REST API."),
        Map.entry("html", "Build a static personal page to learn HTML structure and semantic tags."),
        Map.entry("css", "Style that page with Flexbox and Grid, and make it responsive."),
        Map.entry("javascript", "Learn JavaScript basics and add interactivity (forms, fetch calls) to your web page."),
        Map.entry("react", "Do the official React tutorial, then rebuild one of your pages with components."),
        Map.entry("kotlin", "Take the Kotlin basics course and build a small Android app."),
        Map.entry("python", "Learn Python basics and automate a small task, such as cleaning a CSV file."),
        Map.entry("excel", "Practice formulas, pivot tables and charts on a real dataset."),
        Map.entry("data visualization", "Make charts from a public dataset using Excel, Python (matplotlib) or Power BI."),
        Map.entry("testing", "Learn unit testing (JUnit) and write tests for one of your own projects."),
        Map.entry("networking", "Study TCP/IP basics and set up a small home or lab network."),
        Map.entry("troubleshooting", "Practice diagnosing common PC and network problems step by step."),
        Map.entry("communication", "Present a project to classmates and write a short, clear project summary."),
        Map.entry("attention to detail", "Review your own code or documents with a checklist before submitting them.")
    );

    /** Returns one learning step per missing skill (at most MAX_STEPS). */
    public List<String> recommend(List<String> missing) {
        if (missing == null) return new ArrayList<>();
        return missing.stream().limit(MAX_STEPS).map(this::stepFor).collect(Collectors.toList());
    }

    public String stepFor(String skill) {
        String known = CATALOG.get(Profile.norm(skill));
        return known != null ? known : "Take an online course or build a small project to learn " + skill + ".";
    }
}
