import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.*;

class FreeplayExplorer {
    static int SEED = 12;
    static int START = 300;
    static int END = 310;

    public static void main(String[] args) {
        BloonCalculator bloonCalculator = new BloonCalculator();
        List<JSONObject> freeplayGroups = new ArrayList<>();
        try {
            String text = Files.readString(Path.of("cleanedFreeplayGroups.json"));
            JSONArray tempGroup = new JSONArray(text);
            for (int i = 0; i < tempGroup.length(); i++) {
                freeplayGroups.add(tempGroup.getJSONObject(i));
            }

        } catch (Exception e) {
            System.out.printf("failed to read json file with exception %s", e);
            return;
        }
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
                JSONObject object = freeplayGroups.get(index);
                boolean inBounds = false;
                JSONArray bounds = object.getJSONArray("bounds");
                for (int i = 0; i < bounds.length(); i++) {
                    if ((bounds.getJSONObject(i).getInt("lowerBounds") <= ROUND) && (ROUND <= bounds.getJSONObject(i).getInt("upperBounds"))) {
                        inBounds = true;
                        break;
                    }
                }
                if (!inBounds) {
                    continue;
                }
                float score = object.getFloat("score") == 0 ? calculateScore(object, bloonCalculator) : object.getFloat("score");
                if (score > budget) continue;
                String bloon = object.getJSONObject("group").getString("bloon");
                int count = object.getJSONObject("group").getInt("count");
                roundRBE += (long) bloonCalculator.getRBE(bloon, ROUND) * count;
                roundCash += bloonCalculator.getCash(bloon, ROUND) * count;
                roundTime += object.getJSONObject("group").getInt("end");
                System.out.println(formatEmissions(object));
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

    public static String formatEmissions(JSONObject emission) {
        JSONObject group = emission.getJSONObject("group");
        return String.format("| %16s |%16s |%16s |",
                group.getString("bloon"),
                group.getInt("count"),
                group.getInt("end")
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

    public static float calculateScore(JSONObject bloonModel, BloonCalculator calc) {
        String bloon = bloonModel.getJSONObject("group").getString("bloon");
        int count = bloonModel.getJSONObject("group").getInt("count");
        double multiplier = 1;
        if (bloon.contains("Camo")) {
            multiplier += 0.1;
            bloon = bloon.replace("Camo", "");
        }
        if (bloon.contains("Regrow")) {
            multiplier += 0.1;
            bloon = bloon.replace("Regrow", "");
        }
        int bloonRBE = calc.getRBE(bloon);
        if (count == 1) return (float) (bloonRBE * multiplier);
        double spacing = ((double) bloonModel.getJSONObject("group").getInt("end")) / (60 * count);
        double totalRBE = count * bloonRBE * multiplier;
        if (spacing >= 1) return (float) (totalRBE * 0.8);
        if (spacing >= 0.5) return (float) (totalRBE);
        if (spacing > 0.1) return (float) (totalRBE * 1.1);
        if (spacing > 0.08) return (float) (totalRBE * 1.4);
        return (float) (totalRBE * 1.8);
    }

    /*
    removes the useless fields from the freeplayGroups json
     */
    public static void CleanJSON() {
        List<JSONObject> freeplayGroups = new ArrayList<>();
        try {
            String text = Files.readString(Path.of("freeplayGroups.json"));
            JSONArray tempGroup = new JSONArray(text);
            for (int i = 0; i < tempGroup.length(); i++) {
                freeplayGroups.add(tempGroup.getJSONObject(i));
            }

        } catch (Exception e) {
            System.out.printf("failed to read json file with exception %s", e);
            return;
        }
        JSONArray cleanedJSON = new JSONArray();
        BloonCalculator calc = new BloonCalculator();
        for (JSONObject freeplayGroup : freeplayGroups) {
            /*
            there are some emissions which [group][end] =/= lastEmission.time
            the game respects lastEmission.time so correct it
            example: element 188 in the freeplayGroups.json
            this seems to only happen with single spawn emissions
             */
            JSONArray bloonEmissions = freeplayGroup.getJSONArray("bloonEmissions");
            JSONObject lastEmission = bloonEmissions.getJSONObject(bloonEmissions.length() - 1);
            freeplayGroup.getJSONObject("group").put("end", lastEmission.getInt("time"));


            freeplayGroup.remove("bloonEmissions_"); //its all null
            freeplayGroup.remove("bloonEmissions"); // takes a lot of space with a lot of duplicate information
            // lists out each emission seperately even though they are all the same bloon and evenly spaced
            freeplayGroup.remove("checkedImplementationType"); //all null
            freeplayGroup.remove("ImplementationType"); //why is this one capitalized
            freeplayGroup.remove("implementationType"); //why is this one capitalized
            freeplayGroup.remove("childDependants"); //all null
            freeplayGroup.remove("_name"); // copy of "name"
            freeplayGroup.remove("WasCollected"); // something something but its always false
            freeplayGroup.getJSONObject("group").remove("start"); // always 0
            freeplayGroup.getJSONObject("group").remove("_name"); // copy of "name"
            freeplayGroup.getJSONObject("group").remove("ImplementationType");
            freeplayGroup.getJSONObject("group").remove("implementationType");
            freeplayGroup.getJSONObject("group").remove("childDependants");
            freeplayGroup.getJSONObject("group").remove("checkedImplementationType");
            freeplayGroup.getJSONObject("group").remove("WasCollected");
            if (freeplayGroup.getInt("score") == 0) {
                freeplayGroup.put("score", calculateScore(freeplayGroup, calc));
            }
            cleanedJSON.put(freeplayGroup);
        }
        try {
            FileWriter writer = new FileWriter("cleanedFreeplayGroups.json");
            writer.write(cleanedJSON.toString());
            writer.close();
        } catch (Exception e) {
            System.out.printf("failed to write to cleanFreeplayGroups.json at the cwd, check your permissions %s", e);
        }
    }
}
