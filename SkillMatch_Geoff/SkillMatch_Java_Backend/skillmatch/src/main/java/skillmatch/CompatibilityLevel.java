package skillmatch;

public enum CompatibilityLevel {
    HIGH("High Match"),
    MODERATE("Moderate Match"),
    LOW("Low Match");

    private final String label;

    CompatibilityLevel(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static CompatibilityLevel fromScore(int score) {
        if (score >= 80) {
            return HIGH;
        } else if (score >= 50) {
            return MODERATE;
        }
        return LOW;
    }
}
