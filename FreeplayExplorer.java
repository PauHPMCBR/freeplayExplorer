import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

class FreeplayExplorer {
    static int SEED = 12;
    static int START = 300;
    static int END = 310;

    public static List<FreeplayGroup> parseFreeplayGroup(String filePath) {
        Gson gson = new Gson();
        Type freeplayGroupListType = new TypeToken<List<FreeplayGroup>>() {}.getType();

        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, freeplayGroupListType);
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        List<FreeplayGroup> freeplayGroups = parseFreeplayGroup("cleanedFreeplayGroups.json");

        int ROUND = START;
        long totalRBE = 0;
        double totalCash = 0;
        int totalTime = 0;

        while (ROUND <= END) {
            SeededRandom random = new SeededRandom(ROUND + SEED);
            float budget;
            if (ROUND > 1) {
                budget = (float) (calculateBudget(ROUND) * (1.5 - random.getNext()));
            } else {
                budget = calculateBudget(ROUND);
            }

            float OGBudget = budget;
            long roundRBE = 0;
            double roundCash = 0;
            int roundTime = 0;
            List<Integer> testGroups = IntStream.range(0, 529).boxed().collect(Collectors.toList());
            shuffleSeeded(testGroups, ROUND + SEED);
            System.out.println("+------------------------------------------------------+");
            System.out.printf("| ROUND %46s |\n", ROUND);
            System.out.println("+------------------+-----------------+-----------------+");
            System.out.println("|            Bloon |           Count |          Length |");
            System.out.println("+------------------+-----------------+-----------------+");
            for (int index : testGroups) {
                FreeplayGroup freeplayGroup = freeplayGroups.get(index);
                boolean inBounds = false;
                for (FreeplayGroup.Bounds bounds : freeplayGroup.bounds) {
                    if (bounds.lowerBounds <= ROUND && ROUND <= bounds.upperBounds) {
                        inBounds = true;
                        break;
                    }
                }
                if (!inBounds) {
                    continue;
                }
                if (freeplayGroup.score == 0) freeplayGroup.score = calculateScore(freeplayGroup);
                float score = (float)freeplayGroup.score;
                if (score > budget) continue;
                String bloon = freeplayGroup.group.bloon;
                int count = freeplayGroup.group.count;
                roundRBE += (long) BloonCalculator.getRBE(bloon, ROUND) * count;
                roundCash += BloonCalculator.getCash(bloon, ROUND) * count;
                roundTime += (int) freeplayGroup.group.end;
                System.out.println(formatEmissions(freeplayGroup));
                budget -= score;
            }
            System.out.println("+------------------------------------------------------+");
            System.out.printf("| %52s |\n", String.format("Score budget: %,.2f/%,.2f", OGBudget - budget, OGBudget));
            System.out.printf("| %52s |\n", String.format("Round RBE: %,d", roundRBE));
            System.out.printf("| %52s |\n", String.format("Round Cash: %,.2f", roundCash));
            System.out.printf("| %52s |\n", String.format("Round Length: %,d", roundTime));
            System.out.printf("| %52s |\n", String.format("Health Multiplier: %s", BloonCalculator.getHealthMultiplier(ROUND)));
            System.out.printf("| %52s |\n", String.format("Speed Multiplier: %s", BloonCalculator.getSpeedMultiplier(ROUND)));
            ROUND++;
            totalCash += roundCash;
            totalRBE += roundRBE;
            totalTime += roundTime;
        }
        System.out.println("+------------------------TOTAL-------------------------+");
        System.out.printf("| %52s |\n", String.format("Total RBE: %,d", totalRBE));
        System.out.printf("| %52s |\n", String.format("Total Cash: %,.2f", totalCash));
        System.out.printf("| %52s |\n", String.format("Total Time: %,d", totalTime));
        System.out.println("+------------------------------------------------------+");
    }

    public static String formatEmissions(FreeplayGroup freeplayGroup) {
        return String.format("| %16s |%16s |%16s |",
                freeplayGroup.group.bloon,
                freeplayGroup.group.count,
                freeplayGroup.group.end
        );
    }

    public static <T> void shuffleSeeded(List<T> lst, int seed) {
        SeededRandom random = new SeededRandom(seed);
        int pointer = lst.size() - 1;
        int listLen = pointer;
        while (true) {
            double rand = random.getNextDouble();
            int index = (int) (listLen * rand);
            T temp = lst.get(pointer);
            lst.set(pointer, lst.get(index));
            lst.set(index, temp);
            pointer -= 1;
            if (pointer < 0) {
                return;
            }
        }
    }

    public static float calculateBudget(int round) {
        if (round > 100) {
            return round * 4000 - 225000;
        }
        double budget = Math.pow(round, 7.7);
        double helper = Math.pow(round, 1.75);
        if (round > 50) {
            return (float) (budget * 5e-11 + helper + 20);
        }
        return (float) ((1 + round * 0.01) * (round * -3 + 400) * ((budget * 5e-11 + helper + 20) / 160) * 0.6);
    }

    public static double calculateScore(FreeplayGroup freeplayGroup) {
        String bloon = freeplayGroup.group.bloon;
        int count = freeplayGroup.group.count;
        double multiplier = 1;
        if (bloon.contains("Camo")) {
            multiplier += 0.1;
            bloon = bloon.replace("Camo", "");
        }
        if (bloon.contains("Regrow")) {
            multiplier += 0.1;
            bloon = bloon.replace("Regrow", "");
        }
        int bloonRBE = BloonCalculator.getRBE(bloon);
        if (count == 1) return (float) (bloonRBE * multiplier);
        double spacing = freeplayGroup.group.end / (60 * count);
        double totalRBE = count * bloonRBE * multiplier;
        if (spacing >= 1) return (float) (totalRBE * 0.8);
        if (spacing >= 0.5) return (float) (totalRBE);
        if (spacing > 0.1) return (float) (totalRBE * 1.1);
        if (spacing > 0.08) return (float) (totalRBE * 1.4);
        return totalRBE * 1.8;
    }
}
