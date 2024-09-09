import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;

public class BloonCalculator {
    JSONObject rawText;

    public BloonCalculator() {
        try {
            String text = Files.readString(Path.of("bloonData.json"));
            this.rawText = new JSONObject(text);
        } catch (Exception e) {
            System.out.printf("failed to read json file with exception %s", e);
            System.exit(1);
        }
    }

    public int getCash(String bloon, boolean isSuper) {
        JSONObject bloonData = rawText.getJSONObject(bloon);
        if (bloonData.getBoolean("isMoab")) {
            return bloonData.getInt("cash");
        }
        return bloonData.getInt(isSuper ? "superCash" : "cash");
    }

    public double getCash(String bloon, int round) {
        return getCash(bloon.replace("Fortified", "").replace("Camo", "").replace("Regrow", ""), round > 80) * getCashMultiplier(round);
    }

    public int getRBE(String bloon, double healthMultiplier, boolean isSuper, boolean isFortified) {
        JSONObject bloonData = rawText.getJSONObject(bloon);
        if (bloonData.getBoolean("isMoab")) {
            if (isFortified) {
                return ((int) (bloonData.getInt("sumMoabHealth") * healthMultiplier * 2))
                        + bloonData.getInt("numCeramics") * rawText.getJSONObject("CeramicFortified").getInt(isSuper ? "superRBE" : "RBE");
            } else {
                return ((int) (bloonData.getInt("sumMoabHealth") * healthMultiplier))
                        + bloonData.getInt("numCeramics") * rawText.getJSONObject("Ceramic").getInt(isSuper ? "superRBE" : "RBE");
            }
        }
        if (isFortified) {
            bloonData = rawText.getJSONObject(bloon + "Fortified");
        }
        return bloonData.getInt(isSuper ? "superRBE" : "RBE");
    }


    public int getRBE(String bloon, int round) {
        return getRBE(bloon.replace("Fortified", "").replace("Camo", "").replace("Regrow", ""), getHealthMultiplier(round), round > 80, bloon.contains("Fortified"));
    }

    public int getRBE(String bloon) {
        return getRBE(bloon, 1);
    }

    public static double getHealthMultiplier(int round) {
        if (round <= 80) return 1;
        if (round <= 100) return (round - 30) / 50D;
        if (round <= 124) return (round - 72) / 20D;
        if (round <= 150) return (3 * round - 320) / 20D;
        if (round <= 250) return (7 * round - 920) / 20D;
        if (round <= 300) return round - 208.5;
        if (round <= 400) return (3 * round - 717) / 2D;
        if (round <= 500) return (5 * round - 1517) / 2D;
        return 5 * round - 2008.5;
    }

    public static double getSpeedMultiplier(int round) {
        if (round <= 80) return 1;
        if (round <= 100) return 1 + (round - 80) * 0.02;
        if (round <= 150) return 1.6 + (round - 101) * 0.02;
        if (round <= 200) return 3 + (round - 151) * 0.02;
        if (round <= 250) return 4.5 + (round - 201) * 0.02;
        return 6 + (round - 252) * 0.02;
    }

    public static double getCashMultiplier(int round) {
        if (round <= 50) return 1;
        if (round <= 60) return 0.5;
        if (round <= 85) return 0.2;
        if (round <= 100) return 0.1;
        if (round <= 120) return 0.05;
        return 0.02;
    }
}
