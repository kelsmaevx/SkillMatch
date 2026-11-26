package skillmatch;

import java.util.*;

/** Runs the whole flow for one upload: resume in, top five matches and skill gaps out. Nothing is stored. */
class AnalysisService {
    private final ResumeParser parser; private final AiService ai; private final Recommender recommender;
    private final SkillGapSummarizer gapSummarizer; private final OpportunityRepository repo;
    AnalysisService(ResumeParser p, AiService a, Recommender r, SkillGapSummarizer g, OpportunityRepository repo) {
        parser = p; ai = a; recommender = r; gapSummarizer = g; this.repo = repo;
    }
    AnalysisResult analyze(byte[] file, String location) throws ResumeException {
        AnalysisResult out = new AnalysisResult();
        try {
            String text = parser.extractText(file);
            ResumeResult rr = ai.extractProfile(text, TechCatalog.skills(), new ArrayList<>(repo.tagVocab()));
            out.profile = rr.profile.withLocation(location); out.usedAi = rr.usedAi; out.techRelated = rr.techRelated;
            if (!rr.techRelated) { out.gaps = new GapSummary(); return out; }
            out.results = recommender.recommend(out.profile, 5);
            ai.explain(out.profile, out.results);
            out.gaps = ai.improveGaps(gapSummarizer.summarize(out.results));
            return out;
        } finally { parser.discard(file); }
    }
}
