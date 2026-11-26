package skillmatch;

import java.util.*;

/** Tiny JSON reader/writer (maps, lists, strings, numbers, booleans, null). */
final class Json {
    private final String s; private int i;
    private Json(String s) { this.s = s; }
    static Object parse(String text) { Json j = new Json(text); Object v = j.value(); j.ws(); if (j.i != text.length()) throw j.err("trailing data"); return v; }
    private RuntimeException err(String m) { return new IllegalArgumentException("JSON " + m + " at " + i); }
    private void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
    private Object value() {
        ws(); if (i >= s.length()) throw err("unexpected end");
        char c = s.charAt(i);
        if (c == '{') { i++; Map<String, Object> m = new LinkedHashMap<>(); ws();
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) { ws(); String k = str(); ws(); expect(':'); m.put(k, value()); ws(); if (s.charAt(i) == ',') { i++; continue; } expect('}'); return m; } }
        if (c == '[') { i++; List<Object> l = new ArrayList<>(); ws();
            if (s.charAt(i) == ']') { i++; return l; }
            while (true) { l.add(value()); ws(); if (s.charAt(i) == ',') { i++; continue; } expect(']'); return l; } }
        if (c == '"') return str();
        if (s.startsWith("true", i)) { i += 4; return true; } if (s.startsWith("false", i)) { i += 5; return false; }
        if (s.startsWith("null", i)) { i += 4; return null; }
        int st = i; while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (st == i) throw err("unexpected character"); return Double.parseDouble(s.substring(st, i));
    }
    private void expect(char c) { if (i >= s.length() || s.charAt(i) != c) throw err("expected " + c); i++; }
    private String str() {
        expect('"'); StringBuilder b = new StringBuilder();
        while (i < s.length() && s.charAt(i) != '"') {
            char c = s.charAt(i++);
            if (c != '\\') { b.append(c); continue; }
            char e = s.charAt(i++);
            switch (e) { case 'n' -> b.append('\n'); case 't' -> b.append('\t'); case 'r' -> b.append('\r'); case 'b' -> b.append('\b'); case 'f' -> b.append('\f');
                case 'u' -> { b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; } default -> b.append(e); }
        }
        expect('"'); return b.toString();
    }
    static String write(Object o) { StringBuilder b = new StringBuilder(); w(o, b); return b.toString(); }
    private static void w(Object o, StringBuilder b) {
        if (o == null) b.append("null");
        else if (o instanceof String str) { b.append('"'); for (char c : str.toCharArray()) { switch (c) { case '"' -> b.append("\\\""); case '\\' -> b.append("\\\\");
            case '\n' -> b.append("\\n"); case '\r' -> b.append("\\r"); case '\t' -> b.append("\\t"); default -> { if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c); } } } b.append('"'); }
        else if (o instanceof Number n) { if (n instanceof Double d && d == Math.rint(d) && !Double.isInfinite(d)) b.append(d.longValue()); else b.append(n); }
        else if (o instanceof Boolean) b.append(o);
        else if (o instanceof Map<?, ?> m) { b.append('{'); boolean f = true; for (Map.Entry<?, ?> e : m.entrySet()) { if (!f) b.append(','); f = false; w(String.valueOf(e.getKey()), b); b.append(':'); w(e.getValue(), b); } b.append('}'); }
        else if (o instanceof Collection<?> c) { b.append('['); boolean f = true; for (Object x : c) { if (!f) b.append(','); f = false; w(x, b); } b.append(']'); }
        else w(o.toString(), b);
    }
}
