import java.util.HashMap;
import java.util.Map;

public class BloonData {
    public boolean isMoab;
    public int RBE;
    public int superRBE;
    public int cash;
    public int superCash;
    public int sumMoabHealth;
    public int numCeramics;

    // Constructor for regular bloons
    public BloonData(boolean isMoab, int RBE, int superRBE, int cash, int superCash) {
        this.isMoab = isMoab;
        this.RBE = RBE;
        this.superRBE = superRBE;
        this.cash = cash;
        this.superCash = superCash;
    }

    // Constructor for MOAB-class bloons
    public BloonData(boolean isMoab, int cash, int sumMoabHealth, int numCeramics) {
        this.isMoab = isMoab;
        this.cash = cash;
        this.sumMoabHealth = sumMoabHealth;
        this.numCeramics = numCeramics;
    }

    public static final BloonData CERAMIC = new BloonData(false, 104, 68, 95, 95);
    public static final BloonData CERAMIC_FORTIFIED = new BloonData(false, 114, 128, 95, 95);
    public static final Map<String, BloonData> BLOONS_DATA_MAP = new HashMap<>();

    static {
        BLOONS_DATA_MAP.put("Red", new BloonData(false, 1, 1, 1, 1));
        BLOONS_DATA_MAP.put("Blue", new BloonData(false, 2, 2, 2, 2));
        BLOONS_DATA_MAP.put("Green", new BloonData(false, 3, 3, 3, 3));
        BLOONS_DATA_MAP.put("Yellow", new BloonData(false, 4, 4, 4, 4));
        BLOONS_DATA_MAP.put("Pink", new BloonData(false, 5, 5, 5, 5));
        BLOONS_DATA_MAP.put("Black", new BloonData(false, 11, 6, 11, 6));
        BLOONS_DATA_MAP.put("White", new BloonData(false, 11, 6, 11, 6));
        BLOONS_DATA_MAP.put("Purple", new BloonData(false, 11, 6, 11, 6));
        BLOONS_DATA_MAP.put("Zebra", new BloonData(false, 23, 7, 23, 7));
        BLOONS_DATA_MAP.put("Lead", new BloonData(false, 23, 7, 23, 7));
        BLOONS_DATA_MAP.put("LeadFortified", new BloonData(false, 26, 10, 23, 7));
        BLOONS_DATA_MAP.put("Rainbow", new BloonData(false, 47, 8, 47, 8));
        BLOONS_DATA_MAP.put("Ceramic", CERAMIC);
        BLOONS_DATA_MAP.put("CeramicFortified", CERAMIC_FORTIFIED);

        BLOONS_DATA_MAP.put("Moab", new BloonData(true, 381, 200, 4));
        BLOONS_DATA_MAP.put("Bfb", new BloonData(true, 1525, 1500, 16));
        BLOONS_DATA_MAP.put("Zomg", new BloonData(true, 6101, 10000, 64));
        BLOONS_DATA_MAP.put("Ddt", new BloonData(true, 381, 400, 4));
        BLOONS_DATA_MAP.put("Bad", new BloonData(true, 13346, 41200, 140));
    }
}
