public class BloonCalculator {

    public static int getCash(String bloon, boolean isSuper) {
        BloonData bloonData = BloonData.BLOONS_DATA_MAP.get(bloon);
        if (bloonData.isMoab) return bloonData.cash;
        if (isSuper) return bloonData.superCash;
        return bloonData.cash;
    }

    public static double getCash(String bloon, int round) {
        return getCash(bloon.replace("Fortified", "").replace("Camo", "").replace("Regrow", ""), round > 80) * getCashMultiplier(round);
    }

    public static int getRBE(String bloon, double healthMultiplier, boolean isSuper, boolean isFortified) {
        BloonData bloonData = BloonData.BLOONS_DATA_MAP.get(bloon);
        if (bloonData.isMoab) {
            if (isFortified) {
                return ((int) (bloonData.sumMoabHealth * healthMultiplier * 2))
                        + bloonData.numCeramics * (isSuper ? BloonData.CERAMIC_FORTIFIED.superRBE : BloonData.CERAMIC_FORTIFIED.RBE);
            } else {
                return ((int) (bloonData.sumMoabHealth * healthMultiplier))
                        + bloonData.numCeramics * (isSuper ? BloonData.CERAMIC.superRBE : BloonData.CERAMIC.RBE);
            }
        }
        if (isFortified) {
            bloonData = BloonData.BLOONS_DATA_MAP.get(bloon + "Fortified");
        }
        return (isSuper ? bloonData.superRBE : bloonData.RBE);
    }


    public static int getRBE(String bloon, int round) {
        return getRBE(bloon.replace("Fortified", "").replace("Camo", "").replace("Regrow", ""), getHealthMultiplier(round), round > 80, bloon.contains("Fortified"));
    }

    public static int getRBE(String bloon) {
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
