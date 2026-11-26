package skillmatch;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

class OpportunityRepository {
    private final List<Opportunity> items;
    private OpportunityRepository(List<Opportunity> items) { this.items = List.copyOf(items); }

    @SuppressWarnings("unchecked")
    static OpportunityRepository load(InputStream in) throws Exception {
        List<Object> raw = (List<Object>) Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        List<Opportunity> list = new ArrayList<>(); Set<String> seen = new HashSet<>();
        for (Object r : raw) {
            Map<String, Object> m = (Map<String, Object>) r;
            String type = String.valueOf(m.get("type")), id = String.valueOf(m.get("id"));
            List<String> sk = (List<String>) m.get("skills"), tg = (List<String>) m.getOrDefault("tags", List.of());
            double exp = ((Number) m.getOrDefault("minExperience", 0)).doubleValue();
            Opportunity o = "Internship".equals(type)
                ? new Internship(id, (String) m.get("title"), (String) m.get("company"), (String) m.get("location"), (String) m.get("description"),
                    (String) m.get("minEducation"), exp, sk, tg, ((Number) m.getOrDefault("durationWeeks", 8)).intValue())
                : new Job(id, (String) m.get("title"), (String) m.get("company"), (String) m.get("location"), (String) m.get("description"),
                    (String) m.get("minEducation"), exp, sk, tg, (String) m.get("salary"));
            o.validate();
            if (!seen.add(id)) throw new IllegalArgumentException("Duplicate opportunity id: " + id);
            list.add(o);
        }
        return new OpportunityRepository(list);
    }
    List<Opportunity> all() { return items; }
    Opportunity find(String id) { return items.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null); }
    List<String> locations() {
        TreeSet<String> s = new TreeSet<>(); for (Opportunity o : items) if (!"Remote".equalsIgnoreCase(o.getLocation())) s.add(o.getLocation());
        List<String> out = new ArrayList<>(List.of("Any", "Remote")); out.addAll(s); return out;
    }
    Set<String> tagVocab() { Set<String> s = new TreeSet<>(); for (Opportunity o : items) s.addAll(o.getTags()); return s; }
}
