package skillmatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingRecommender {

    static final int MAX_STEPS = 3;

    private static final Map<String, String> catalog = new HashMap<>();

    static {
        catalog.put("java", "Finish a Java OOP course and build a simple program like a library system.");
        catalog.put("oop", "Practice the four OOP concepts by making a small program using classes and objects.");
        catalog.put("sql", "Learn SELECT, JOIN and GROUP BY, then try making a small database.");
        catalog.put("git", "Practice using commits, branches and pull requests on GitHub.");
        catalog.put("spring", "Learn the basics of Spring Boot and try making a simple REST API.");
        catalog.put("html", "Make a simple webpage to practice HTML.");
        catalog.put("css", "Practice CSS by adding styles and layouts to your webpage.");
        catalog.put("javascript", "Learn JavaScript basics and add simple actions to your webpage.");
        catalog.put("react", "Learn React components and try making a small webpage.");
        catalog.put("kotlin", "Learn Kotlin basics and try making a simple Android app.");
        catalog.put("python", "Learn Python basics and make a small program.");
        catalog.put("excel", "Practice Excel formulas, tables and charts.");
        catalog.put("data visualization", "Practice making charts using Excel or Python.");
        catalog.put("testing", "Learn how to test your Java programs using JUnit.");
        catalog.put("networking", "Study the basics of computer networks and TCP/IP.");
        catalog.put("troubleshooting", "Practice finding and fixing common computer problems.");
        catalog.put("communication", "Practice explaining your projects clearly.");
        catalog.put("attention to detail", "Review your code using a simple checklist before submitting.");
    }

    // Gives learning suggestions for missing skills
    public List<String> recommend(List<String> missing) {
        List<String> suggestions = new ArrayList<>();

        if (missing == null) {
            return suggestions;
        }

        for (int i = 0; i < missing.size() && i < MAX_STEPS; i++) {
            suggestions.add(stepFor(missing.get(i)));
        }

        return suggestions;
    }

    public String stepFor(String skill) {
        String key = Profile.norm(skill);
        String suggestion = catalog.get(key);

        if (suggestion != null) {
            return suggestion;
        }

        return "Take an online lesson or make a small project to learn " + skill + ".";
    }
}
