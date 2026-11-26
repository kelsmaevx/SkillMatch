package skillmatch;

import java.io.IOException;
import java.util.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/** PDFBox adapter: text plus positioned word boxes for the first 10 pages, all in memory. */
final class PdfBoxReader {
    static PdfContent read(byte[] data) throws ResumeException {
        try (PDDocument doc = Loader.loadPDF(data)) {
            if (doc.isEncrypted() && !doc.getCurrentAccessPermission().canExtractContent())
                throw new ResumeException("PASSWORD", "The PDF is password-protected.");
            List<PageLayout> pages = new ArrayList<>();
            PDFTextStripper st = new PDFTextStripper() {
                private double width; private List<double[]> boxes;
                @Override protected void startPage(PDPage p) throws IOException { width = p.getMediaBox().getWidth(); boxes = new ArrayList<>(); super.startPage(p); }
                @Override protected void endPage(PDPage p) throws IOException { pages.add(new PageLayout(width, boxes)); super.endPage(p); }
                @Override protected void writeString(String t, List<TextPosition> pos) throws IOException {
                    if (!pos.isEmpty() && !t.isBlank()) { TextPosition a = pos.get(0), z = pos.get(pos.size() - 1);
                        boxes.add(new double[]{a.getXDirAdj(), z.getXDirAdj() + z.getWidthDirAdj(), a.getYDirAdj()}); }
                    super.writeString(t, pos);
                }
            };
            st.setStartPage(1); st.setEndPage(10); st.setSortByPosition(false);
            return new PdfContent(st.getText(doc), pages);
        } catch (InvalidPasswordException e) { throw new ResumeException("PASSWORD", "The PDF is password-protected.");
        } catch (ResumeException e) { throw e;
        } catch (IOException e) { throw new ResumeException("CORRUPT", "The PDF is damaged and could not be opened."); }
    }
}
