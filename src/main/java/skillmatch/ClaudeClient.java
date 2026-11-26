package skillmatch;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/** Sends requests to the Claude API. The key comes from ANTHROPIC_API_KEY; without it, available() is false. */
class ClaudeClient {
    private final String apiKey, model;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    ClaudeClient(String apiKey, String model) { this.apiKey = apiKey == null ? "" : apiKey.trim(); this.model = model; }
    boolean available() { return !apiKey.isEmpty(); }

    String ask(String system, String user) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model); body.put("max_tokens", 2000); body.put("system", system);
        body.put("messages", List.of(Map.of("role", "user", "content", user)));
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.anthropic.com/v1/messages")).timeout(Duration.ofSeconds(40))
            .header("x-api-key", apiKey).header("anthropic-version", "2023-06-01").header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(Json.write(body))).build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Claude API returned " + res.statusCode());
        StringBuilder out = new StringBuilder();
        for (Object c : (List<?>) ((Map<?, ?>) Json.parse(res.body())).get("content"))
            if (c instanceof Map<?, ?> m && "text".equals(m.get("type"))) out.append(m.get("text"));
        return out.toString();
    }
    /** Strips code fences and any text around the outermost JSON object or array. */
    static String clean(String s) {
        int a = s.indexOf('{'), b = s.lastIndexOf('}'), c = s.indexOf('['), d = s.lastIndexOf(']');
        if (c >= 0 && (a < 0 || c < a)) return s.substring(c, d + 1);
        return a >= 0 ? s.substring(a, b + 1) : s.trim();
    }
}
