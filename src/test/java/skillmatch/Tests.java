package skillmatch;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Dependency-free checks: run with `java -cp target/test-classes:target/classes skillmatch.Tests`. */
public class Tests {
    static int pass, fail;
    static void check(String name, boolean ok) { if (ok) pass++; else fail++; System.out.println((ok ? "PASS  " : "FAIL  ") + name); }

    static byte[] docx(String bodyXml) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(bo)) {
            z.putNextEntry(new ZipEntry("word/document.xml"));
            z.write(("<?xml version=\"1.0\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>" + bodyXml + "</w:body></w:document>").getBytes());
            z.closeEntry();
        }
        return bo.toByteArray();
    }
    static String p(String t) { return "<w:p><w:r><w:t>" + t + "</w:t></w:r></w:p>"; }
    static final String GOOD = p("Jane Cruz") + p("SKILLS") + p("Java, SQL, Git, HTML, CSS, JavaScript, Linux, Python") + p("EXPERIENCE")
        + p("Intern, Acme 2024 - 2025") + p("EDUCATION") + p("Bachelor of Science in Computer Science") + p("INTERESTS") + p("Web Development, Backend");
    static String code(byte[] d) { try { new ResumeParser().extractText(d); return "OK"; } catch (ResumeException e) { return e.code; } }

    public static void main(String[] a) throws Exception {
        // --- scoring
        MatchingEngine eng = new MatchingEngine();
        Opportunity job = new Job("t", "Java Developer", "X", "Batangas City", "", "Bachelor's", 1, List.of("Java", "SQL", "Git", "Docker"), List.of("Backend"), "");
        Profile full = new Profile(List.of("java", "SQL", "Git", "Docker"), List.of("Backend"), "Bachelor of Science", "Batangas City", "Job", 2);
        check("perfect profile scores 100 / Strong", eng.match(full, job).score == 100 && eng.match(full, job).level.equals("Strong Match"));
        Profile weak = new Profile(List.of("Java"), List.of(), "", "Cebu", "Internship", 0);
        MatchResult w = eng.match(weak, job);
        check("weak profile: missing skills listed, score <40 = Low", w.missing.equals(List.of("SQL", "Git", "Docker")) && w.score < 40 && w.level.equals("Low Match"));
        check("breakdown sums to score", w.breakdown.values().stream().mapToInt(Integer::intValue).sum() == w.score);
        check("level boundaries", CompatibilityLevel.fromScore(80) == CompatibilityLevel.STRONG && CompatibilityLevel.fromScore(79) == CompatibilityLevel.GOOD
            && CompatibilityLevel.fromScore(59) == CompatibilityLevel.FAIR && CompatibilityLevel.fromScore(39) == CompatibilityLevel.LOW);
        Opportunity intern = new Internship("i", "Dev Intern", "X", "Remote", "", "Bachelor's", 3, List.of("Java"), List.of(), 8);
        check("Internship caps required experience at 0.5 (polymorphism)", intern.requiredExperience() == 0.5 && job.requiredExperience() == 1);
        check("unknown education falls back to High School", new Profile(null, null, "???", "", "", 0).getEducation().equals("High School"));
        OpportunityRepository repo = OpportunityRepository.load(Tests.class.getResourceAsStream("/opportunities.json"));
        check("dataset loads and all records are tech", repo.all().size() >= 20 && repo.locations().containsAll(List.of("Any", "Remote", "Batangas City")));
        try { new Job("x", "Barista", "X", "Batangas City", "", "High School", 0, List.of("Latte Art"), List.of(), "").validate(); check("non-tech opportunity rejected", false); }
        catch (IllegalArgumentException e) { check("non-tech opportunity rejected", true); }

        // --- layout analyzer (synthetic): two columns vs one column with right-aligned dates
        List<double[]> two = new ArrayList<>(), one = new ArrayList<>();
        for (int i = 0; i < 30; i++) { two.add(new double[]{40, 270, 50 + i * 14}); two.add(new double[]{330, 560, 50 + i * 14}); one.add(new double[]{40, 540, 50 + i * 14}); }
        for (int i = 0; i < 6; i++) { one.add(new double[]{500, 560, 50 + i * 28}); }
        check("analyzer: two-column page detected", LayoutAnalyzer.isMultiColumn(new PageLayout(612, two)));
        check("analyzer: one-column page (with right-aligned dates) accepted", !LayoutAnalyzer.isMultiColumn(new PageLayout(612, one)));
        List<double[]> side = new ArrayList<>(); for (int i = 0; i < 30; i++) { side.add(new double[]{20, 150, 50 + i * 14}); side.add(new double[]{200, 580, 50 + i * 14}); }
        check("analyzer: narrow sidebar layout detected", LayoutAnalyzer.isMultiColumn(new PageLayout(612, side)));

        // --- parser
        check("DOCX one column accepted", code(docx(GOOD)).equals("OK"));
        check("DOCX w:cols num=2 -> MULTI_COLUMN", code(docx("<w:sectPr><w:cols w:num=\"2\"/></w:sectPr>" + GOOD)).equals("MULTI_COLUMN"));
        check("DOCX text box sidebar -> MULTI_COLUMN", code(docx("<w:p><w:r><w:txbxContent>" + p("SKILLS Java Python SQL Git Docker Linux Kubernetes AWS") + "</w:txbxContent></w:r></w:p>" + GOOD)).equals("MULTI_COLUMN"));
        check("DOCX layout table -> MULTI_COLUMN", code(docx("<w:tbl><w:tr><w:tc>" + p("SKILLS Java SQL Git Linux Python Docker HTML CSS JavaScript") + "</w:tc><w:tc>" + p("EXPERIENCE Intern at Acme building web apps EDUCATION BS Computer Science") + "</w:tc></w:tr></w:tbl>" + p("Jane"))).equals("MULTI_COLUMN"));
        check("DOCX with no resume sections -> NOT_RESUME_FORMAT", code(docx(p("Hello this is a long letter about my holiday that has no resume headings at all, just some ordinary sentences."))).equals("NOT_RESUME_FORMAT"));
        check("tiny/scanned -> NO_TEXT", code(docx(p("hi"))).equals("NO_TEXT"));
        check("empty -> EMPTY", code(new byte[0]).equals("EMPTY"));
        check("txt -> WRONG_TYPE", code("plain text resume".getBytes()).equals("WRONG_TYPE"));
        check("old .doc -> OLD_DOC", code(new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, 1, 2, 3}).equals("OLD_DOC"));
        check("corrupt zip -> CORRUPT/WRONG_TYPE", Set.of("CORRUPT", "WRONG_TYPE").contains(code("PK\u0003\u0004garbage".getBytes())));
        check("over 5 MB -> TOO_LARGE", code(new byte[6 * 1024 * 1024]).equals("TOO_LARGE"));
        byte[] zeroed = docx(GOOD); new ResumeParser().discard(zeroed);
        check("discard() overwrites bytes with zeros", Arrays.equals(zeroed, new byte[zeroed.length]));

        // --- shipped template must itself be accepted, and produce a tech profile
        byte[] tpl = Files.readAllBytes(Path.of("src/main/resources/web/templates/skillmatch-resume-template.docx"));
        check("shipped sample template passes the one-column check", code(tpl.clone()).equals("OK"));

        // --- end to end over HTTP (no API key => keyword fallback)
        Main app = Main.build(""); com.sun.net.httpserver.HttpServer srv = app.start(0); int port = srv.getAddress().getPort();
        HttpClient http = HttpClient.newHttpClient(); String base = "http://localhost:" + port;
        HttpResponse<String> r1 = post(http, base, "Batangas City", docx(GOOD));
        Map<?, ?> j1 = (Map<?, ?>) Json.parse(r1.body());
        check("HTTP 200 + five ranked results + gaps", r1.statusCode() == 200 && ((List<?>) j1.get("results")).size() == 5 && j1.get("gaps") != null && Boolean.FALSE.equals(j1.get("usedAi")));
        List<?> rs = (List<?>) j1.get("results"); int s0 = ((Number) ((Map<?, ?>) rs.get(0)).get("score")).intValue(), s4 = ((Number) ((Map<?, ?>) rs.get(4)).get("score")).intValue();
        check("results sorted best-first", s0 >= s4); System.out.println("      top: " + ((Map<?, ?>) rs.get(0)).get("title") + " " + s0 + "%  | skills read: " + ((Map<?, ?>) j1.get("profile")).get("skills"));
        HttpResponse<String> r2 = post(http, base, "Batangas City", docx("<w:sectPr><w:cols w:num=\"2\"/></w:sectPr>" + GOOD));
        Map<?, ?> e2 = (Map<?, ?>) ((Map<?, ?>) Json.parse(r2.body())).get("error");
        check("two-column upload -> 422 + template link", r2.statusCode() == 422 && "MULTI_COLUMN".equals(e2.get("code")) && "/templates/skillmatch-resume-template.docx".equals(e2.get("template")));
        HttpResponse<byte[]> t = http.send(HttpRequest.newBuilder(URI.create(base + "/templates/skillmatch-resume-template.docx")).build(), HttpResponse.BodyHandlers.ofByteArray());
        check("template downloads (200, valid zip)", t.statusCode() == 200 && t.body()[0] == 'P' && t.body()[1] == 'K');
        check("missing location -> 400", post(http, base, "", docx(GOOD)).statusCode() == 400);
        check("static page served", http.send(HttpRequest.newBuilder(URI.create(base + "/")).build(), HttpResponse.BodyHandlers.ofString()).statusCode() == 200);
        check("path traversal blocked", http.send(HttpRequest.newBuilder(URI.create(base + "/..%2f..%2fetc/passwd")).build(), HttpResponse.BodyHandlers.ofString()).statusCode() != 200);
        srv.stop(0);
        System.out.println("\n" + pass + " passed, " + fail + " failed"); System.exit(fail == 0 ? 0 : 1);
    }

    static HttpResponse<String> post(HttpClient h, String base, String loc, byte[] file) throws Exception {
        String b = "----T" + System.nanoTime(); ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(("--" + b + "\r\nContent-Disposition: form-data; name=\"location\"\r\n\r\n" + loc + "\r\n--" + b + "\r\nContent-Disposition: form-data; name=\"resume\"; filename=\"r.docx\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes());
        o.write(file); o.write(("\r\n--" + b + "--\r\n").getBytes());
        return h.send(HttpRequest.newBuilder(URI.create(base + "/api/analyze")).header("Content-Type", "multipart/form-data; boundary=" + b).POST(HttpRequest.BodyPublishers.ofByteArray(o.toByteArray())).build(), HttpResponse.BodyHandlers.ofString());
    }
}
