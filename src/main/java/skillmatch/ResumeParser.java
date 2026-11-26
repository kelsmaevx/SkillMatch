package skillmatch;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** A resume that cannot be read. The website answers with a message plus the sample template. */
class ResumeException extends Exception {
    final String code;
    ResumeException(String code, String message) { super(message); this.code = code; }
}

/** One page of positioned text boxes {x0, x1, y} used to look for a column gutter. */
record PageLayout(double width, List<double[]> boxes) {}
record PdfContent(String text, List<PageLayout> pages) {}

/** Decides whether text is laid out in more than one column. */
final class LayoutAnalyzer {
    /** True when a vertical gap splits many lines into a left and a right part (columns or sidebar). */
    static boolean isMultiColumn(PageLayout page) {
        Map<Long, List<double[]>> lines = new HashMap<>();
        for (double[] b : page.boxes()) lines.computeIfAbsent(Math.round(b[2] / 4.0), k -> new ArrayList<>()).add(b);
        int L = lines.size(); if (L < 8 || page.width() <= 0) return false;
        final int BINS = 100; int[] cover = new int[BINS]; List<boolean[]> rows = new ArrayList<>();
        for (List<double[]> line : lines.values()) {
            boolean[] row = new boolean[BINS];
            for (double[] b : line) for (int k = (int) Math.max(0, b[0] / page.width() * BINS); k <= Math.min(BINS - 1, (int) (b[1] / page.width() * BINS)); k++) row[k] = true;
            rows.add(row); for (int k = 0; k < BINS; k++) if (row[k]) cover[k]++;
        }
        int k = 15;
        while (k < 85) {
            if (cover[k] > 0.03 * L) { k++; continue; }
            int s = k; while (k < 85 && cover[k] <= 0.03 * L) k++;
            if (k - s < 3) continue;
            int both = 0;
            for (boolean[] r : rows) { boolean left = false, right = false;
                for (int j = 0; j < s; j++) left |= r[j]; for (int j = k; j < BINS; j++) right |= r[j];
                if (left && right) both++; }
            if (both >= 0.25 * L) return true;
        }
        return false;
    }
}

/** Reads PDF/DOCX bytes in memory only; rejects anything that is not a readable one-column resume. */
class ResumeParser {
    static final int MAX_BYTES = 5 * 1024 * 1024, MAX_CHARS = 15000;
    static final java.util.regex.Pattern HEADING = java.util.regex.Pattern.compile(
        "(?im)^\\s*(summary|objective|profile|skills|technical skills|core competencies|proficiencies|experience|work experience|work history|employment|projects|education|academic background|certifications|interests|preferred opportunity|achievements|training)\\s*:?\\s*$");

    String extractText(byte[] d) throws ResumeException {
        if (d == null || d.length == 0) throw new ResumeException("EMPTY", "The file is empty.");
        if (d.length > MAX_BYTES) throw new ResumeException("TOO_LARGE", "The file is larger than 5 MB.");
        String text;
        if (d.length > 4 && d[0] == '%' && d[1] == 'P' && d[2] == 'D' && d[3] == 'F') text = fromPdf(d);
        else if (d.length > 4 && d[0] == 'P' && d[1] == 'K') text = fromDocx(d);
        else if (d.length > 4 && (d[0] & 0xFF) == 0xD0 && (d[1] & 0xFF) == 0xCF) throw new ResumeException("OLD_DOC", "Old .doc files are not supported. Save it as .docx or PDF.");
        else throw new ResumeException("WRONG_TYPE", "Only PDF and DOCX resumes are supported.");
        text = text.replace('\u00a0', ' ').strip();
        if (text.length() > MAX_CHARS) text = text.substring(0, MAX_CHARS);
        if (text.length() < 80) throw new ResumeException("NO_TEXT", "No readable text was found. The file may be scanned or image-only.");
        if (HEADING.matcher(text).results().map(m -> m.group(1).toLowerCase()).distinct().count() < 2)
            throw new ResumeException("NOT_RESUME_FORMAT", "We could not find standard resume sections (such as Skills, Experience, Education).");
        return text;
    }

    private String fromPdf(byte[] d) throws ResumeException {
        PdfContent c = PdfBoxReader.read(d);
        for (PageLayout p : c.pages()) if (LayoutAnalyzer.isMultiColumn(p))
            throw new ResumeException("MULTI_COLUMN", "This resume uses more than one column.");
        return c.text();
    }

    private String fromDocx(byte[] d) throws ResumeException {
        byte[] xml = null;
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(d))) {
            for (ZipEntry e; (e = z.getNextEntry()) != null; ) if (e.getName().equals("word/document.xml")) { xml = z.readNBytes(MAX_BYTES * 4); break; }
        } catch (Exception e) { throw new ResumeException("CORRUPT", "The file is damaged and could not be opened."); }
        if (xml == null) throw new ResumeException("WRONG_TYPE", "This is not a Word (.docx) document.");
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance(); f.setNamespaceAware(true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = f.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
            String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
            NodeList cols = doc.getElementsByTagNameNS(W, "cols");
            for (int i = 0; i < cols.getLength(); i++) { String n = ((Element) cols.item(i)).getAttributeNS(W, "num");
                if (!n.isEmpty() && Integer.parseInt(n) >= 2) throw new ResumeException("MULTI_COLUMN", "This resume uses more than one column."); }
            NodeList boxes = doc.getElementsByTagNameNS(W, "txbxContent");
            for (int i = 0; i < boxes.getLength(); i++) if (boxes.item(i).getTextContent().strip().length() > 40)
                throw new ResumeException("MULTI_COLUMN", "This resume uses text boxes or side panels.");
            StringBuilder sb = new StringBuilder(); NodeList ps = doc.getElementsByTagNameNS(W, "p");
            for (int i = 0; i < ps.getLength(); i++) { sb.append(paragraph(ps.item(i), W)).append('\n'); }
            int total = Math.max(1, sb.length()); NodeList tbls = doc.getElementsByTagNameNS(W, "tbl");
            for (int i = 0; i < tbls.getLength(); i++) { Element t = (Element) tbls.item(i); int maxCells = 0;
                NodeList rows = t.getElementsByTagNameNS(W, "tr");
                for (int r = 0; r < rows.getLength(); r++) maxCells = Math.max(maxCells, ((Element) rows.item(r)).getElementsByTagNameNS(W, "tc").getLength());
                if (maxCells >= 2 && t.getTextContent().length() > 0.35 * total) throw new ResumeException("MULTI_COLUMN", "This resume is laid out in a table."); }
            return sb.toString();
        } catch (ResumeException e) { throw e; }
        catch (Exception e) { throw new ResumeException("CORRUPT", "The file is damaged and could not be read."); }
    }

    private static String paragraph(Node p, String W) {
        StringBuilder b = new StringBuilder();
        for (Node n = p.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (!(n instanceof Element e)) continue;
            if (e.getLocalName().equals("r") || e.getLocalName().equals("hyperlink")) {
                for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
                    if (c instanceof Element ce) { String ln = ce.getLocalName();
                        if (ln.equals("t")) b.append(ce.getTextContent()); else if (ln.equals("tab")) b.append('\t'); else if (ln.equals("br")) b.append('\n');
                        else if (ln.equals("r")) b.append(ce.getTextContent()); } } } }
        return b.toString();
    }

    /** Overwrites the uploaded bytes with zeros once the request is done. */
    void discard(byte[] data) { if (data != null) Arrays.fill(data, (byte) 0); }
}
