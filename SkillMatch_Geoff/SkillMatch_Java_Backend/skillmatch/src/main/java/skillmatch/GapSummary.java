package skillmatch;

import java.util.ArrayList;
import java.util.List;

public class GapSummary {
    private List<GapItem> items;
    private int matchedCount;
    private int missingCount;

    public GapSummary(List<GapItem> items, int matchedCount, int missingCount) {
        this.items = items == null ? new ArrayList<GapItem>() : new ArrayList<>(items);
        this.matchedCount = matchedCount;
        this.missingCount = missingCount;
    }

    public List<GapItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public int getMissingCount() {
        return missingCount;
    }

    public int getTotalCount() {
        return matchedCount + missingCount;
    }
}
