package skillmatch;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

public class Main {
    static final String TEMPLATE_URL = "/templates/skillmatch-resume-template.docx";
    static final int MAX_BODY = ResumeParser.MAX_BYTES + 512 * 1024;
    private final AnalysisService service; private final OpportunityRepository repo;

    Main(AnalysisService s, OpportunityRepository r) { service = s; repo = r; }

    static Main build(String apiKey) throws Exception {
        OpportunityRepository repo = OpportunityRepository.load(Main.class.getResourceAsStream("/opportunities.json"));
        ResumeAnalyzer analyzer = new ResumeAnalyzer(); TrainingRecommender training = new TrainingRecommender();
        AiService ai = new AiService(new ClaudeClient(apiKey, System.getenv().getOrDefault("CLAUDE_MODEL", "claude-sonnet-5-5")), analyzer);
        Recommender rec = new RecommendationEngine(new MatchingEngine(), repo, training);
        return new Main(new AnalysisService(new ResumeParser(), ai, rec, new SkillGapSummarizer(training), repo), repo);
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", args.length > 0 ? args[0] : "8080"));
        String key = System.getenv("ANTHROPIC_API_KEY");
        Main app = build(key); HttpServer s = app.start(port);
        System.out.println("SkillMatch running at http://localhost:" + s.getAddress().getPort() + (key == null || key.isBlank() ? "  (no ANTHROPIC_API_KEY: keyword fallback)" : "  (Claude enabled)"));
    }

    HttpServer start(int port) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress(port), 0);
        s.createContext("/", this::staticFile); s.createContext("/api/locations", ex -> send(ex, 200, Map.of("locations", repo.locations())));
        s.createContext("/api/analyze", this::analyze); s.setExecutor(Executors.newFixedThreadPool(8)); s.start(); return s;
    }

    private void analyze(HttpExchange ex) throws IOException {
        try {
            if (!ex.getRequestMethod().equals("POST")) { send(ex, 405, error("BAD_REQUEST", "Use POST.", false)); return; }
            String ct = ex.getRequestHeaders().getFirst("Content-Type");
            if (ct == null || !ct.toLowerCase().startsWith("multipart/form-data") || !ct.contains("boundary=")) { send(ex, 400, error("BAD_REQUEST", "Send the resume as multipart form data.", false)); return; }
            byte[] body = ex.getRequestBody().readNBytes(MAX_BODY + 1);
            if (body.length > MAX_BODY) { send(ex, 413, error("TOO_LARGE", "The file is larger than 5 MB.", true)); return; }
            Map<String, byte[]> parts = Multipart.parse(body, ct.substring(ct.indexOf("boundary=") + 9).replace("\"", "").trim());
            String loc = parts.containsKey("location") ? new String(parts.get("location"), "UTF-8").trim() : "";
            if (loc.isEmpty() || !repo.locations().contains(loc)) { send(ex, 400, error("BAD_REQUEST", "Please choose a location.", false)); return; }
            if (!parts.containsKey("resume")) { send(ex, 400, error("BAD_REQUEST", "Please upload your resume.", false)); return; }
            AnalysisResult r = service.analyze(parts.get("resume"), loc);
            send(ex, 200, toJson(r));
        } catch (ResumeException e) { send(ex, e.code.equals("TOO_LARGE") ? 413 : 422, error(e.code, e.getMessage(), true));
        } catch (Exception e) { send(ex, 500, error("SERVER_ERROR", "Something went wrong. Please try again.", false)); }
    }

    private static Map<String, Object> error(String code, String msg, boolean template) {
        Map<String, Object> e = new LinkedHashMap<>(); e.put("code", code); e.put("message", msg);
        if (template) e.put("template", TEMPLATE_URL);
        return Map.of("error", e);
    }

    private Map<String, Object> toJson(AnalysisResult r) {
        Map<String, Object> o = new LinkedHashMap<>(); o.put("usedAi", r.usedAi); o.put("techRelated", r.techRelated);
        Map<String, Object> p = new LinkedHashMap<>(); p.put("skills", r.profile.getSkills()); p.put("interests", r.profile.getInterests());
        p.put("education", r.profile.getEducation()); p.put("experienceYears", r.profile.getExperienceYears()); p.put("preferredType", r.profile.getPreferredType()); p.put("location", r.profile.getLocation());
        o.put("profile", p);
        List<Object> res = new ArrayList<>();
        for (MatchResult m : r.results) { Opportunity op = repo.find(m.id); Map<String, Object> x = new LinkedHashMap<>();
            x.put("id", m.id); x.put("title", m.title); x.put("company", op.getCompany()); x.put("location", op.getLocation()); x.put("type", op.getType());
            x.put("score", m.score); x.put("level", m.level); x.put("matched", m.matched); x.put("missing", m.missing); x.put("breakdown", m.breakdown);
            x.put("explanation", m.explanation); x.put("training", m.training); res.add(x); }
        o.put("results", res);
        Map<String, Object> g = new LinkedHashMap<>(); List<Object> items = new ArrayList<>();
        for (GapItem i : r.gaps.items) items.add(Map.of("skill", i.skill, "missingIn", i.missingIn, "outOf", i.outOf, "advice", i.advice));
        g.put("items", items); g.put("summary", r.gaps.summary); o.put("gaps", g);
        return o;
    }

    private void staticFile(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath(); if (path.equals("/")) path = "/homepage.html";
        if (path.contains("..") || path.contains("\\")) { ex.sendResponseHeaders(400, -1); ex.close(); return; }
        try (InputStream in = Main.class.getResourceAsStream("/web" + path)) {
            if (in == null) { byte[] nf = "Not found".getBytes(); ex.sendResponseHeaders(404, nf.length); ex.getResponseBody().write(nf); ex.close(); return; }
            byte[] b = in.readAllBytes(); String ext = path.substring(path.lastIndexOf('.') + 1);
            String type = Map.of("html", "text/html; charset=utf-8", "css", "text/css", "js", "text/javascript", "png", "image/png", "jpg", "image/jpeg",
                "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document").getOrDefault(ext, "application/octet-stream");
            ex.getResponseHeaders().set("Content-Type", type);
            if (ext.equals("docx")) ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"SkillMatch-Resume-Template.docx\"");
            ex.sendResponseHeaders(200, b.length); ex.getResponseBody().write(b); ex.close();
        }
    }

    private static void send(HttpExchange ex, int code, Object json) throws IOException {
        byte[] b = Json.write(json).getBytes("UTF-8");
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, b.length); ex.getResponseBody().write(b); ex.close();
    }
}

final class Multipart {
    static Map<String, byte[]> parse(byte[] body, String boundary) {
        Map<String, byte[]> out = new HashMap<>(); byte[] delim = ("--" + boundary).getBytes();
        int pos = indexOf(body, delim, 0);
        while (pos >= 0) {
            int start = pos + delim.length; if (start + 2 > body.length || (body[start] == '-' && body[start + 1] == '-')) break;
            start += 2; int next = indexOf(body, delim, start); if (next < 0) break;
            int hdrEnd = indexOf(body, "\r\n\r\n".getBytes(), start);
            if (hdrEnd > 0 && hdrEnd < next) {
                String hdr = new String(body, start, hdrEnd - start); java.util.regex.Matcher m = java.util.regex.Pattern.compile("name=\"([^\"]*)\"").matcher(hdr);
                if (m.find()) out.put(m.group(1), Arrays.copyOfRange(body, hdrEnd + 4, next - 2));
            }
            pos = next;
        }
        return out;
    }
    private static int indexOf(byte[] a, byte[] p, int from) {
        outer: for (int i = from; i <= a.length - p.length; i++) { for (int j = 0; j < p.length; j++) if (a[i + j] != p[j]) continue outer; return i; }
        return -1;
    }
}
