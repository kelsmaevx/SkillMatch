package skillmatch;

import java.util.*;
import java.util.regex.Pattern;

/** Keyword-based profile reading, used when Claude is unavailable, and the tech filter. */
class ResumeAnalyzer {
    Profile analyze(String text, List<String> skillVocab, List<String> tagVocab) {
        List<String> skills = keepRelevant(keywordMatch(text, skillVocab), skillVocab);
        List<String> interests = keywordMatch(text, tagVocab);
        String edu = ""; java.util.regex.Matcher m = Pattern.compile("(?im)^.*(ph\\.?d|doctor|master|bachelor|b\\.?s\\.?\\b|vocational|diploma|tesda|senior high).*$").matcher(sectionText(text, "education|academic background"));
        if (m.find()) edu = m.group(1);
        String pref = "Any";  java.util.regex.Matcher pm = Pattern.compile("(?i)preferred opportunity\\W+(internship|job|any)").matcher(text);
        if (pm.find()) pref = pm.group(1);
        return new Profile(skills, interests, edu, "", pref, years(sectionText(text, "experience|work experience|work history|employment")));
    }

    List<String> keywordMatch(String text, List<String> vocab) {
        List<String> out = new ArrayList<>();
        for (String term : vocab) {
            String q = Pattern.quote(term);
            String re = term.equals("C") ? "(?<![A-Za-z0-9+#.])C(?![A-Za-z0-9+#.])(?=\\s*[,;|/•\\n]|\\s*$)"
                : "(?i)(?<![A-Za-z0-9+#.])" + q + "(?![A-Za-z0-9+#])";
            if (Pattern.compile(re, Pattern.MULTILINE).matcher(text).find()) out.add(term);
        }
        return out;
    }
    List<String> keepRelevant(List<String> skills, List<String> vocab) {
        Set<String> v = new HashSet<>(); for (String s : vocab) v.add(Profile.norm(s));
        List<String> out = new ArrayList<>(); for (String s : skills) if (v.contains(Profile.norm(s)) && !out.contains(s)) out.add(s);
        return out;
    }
    boolean isTechRelated(Profile p) { return TechCatalog.hasTechSkill(p.getSkills()); }

    /** Text under a heading, up to the next recognised heading (or the whole text if none). */
    static String sectionText(String text, String names) {
        java.util.regex.Matcher h = Pattern.compile("(?im)^\\s*(" + names + ")\\s*:?\\s*$").matcher(text);
        if (!h.find()) return "";
        int start = h.end(); java.util.regex.Matcher n = ResumeParser.HEADING.matcher(text); int end = text.length();
        while (n.find()) if (n.start() >= start) { end = n.start(); break; }
        return text.substring(start, end);
    }
    private static double years(String exp) {
        double sum = 0; java.util.regex.Matcher m = Pattern.compile("((?:19|20)\\d\\d)\\s*(?:-|–|—|to)\\s*((?:19|20)\\d\\d|present|current)", Pattern.CASE_INSENSITIVE).matcher(exp);
        int now = java.time.Year.now().getValue();
        while (m.find()) { int a = Integer.parseInt(m.group(1)), b = Character.isDigit(m.group(2).charAt(0)) ? Integer.parseInt(m.group(2)) : now; sum += Math.max(0.25, b - a); }
        java.util.regex.Matcher y = Pattern.compile("(\\d+(?:\\.\\d+)?)\\+?\\s*years?", Pattern.CASE_INSENSITIVE).matcher(exp);
        double stated = 0; while (y.find()) stated = Math.max(stated, Double.parseDouble(y.group(1)));
        return Math.min(40, Math.max(sum, stated));
    }
}
