import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

class FreeplayExplorer {

    static List<FreeplayGroup> freeplayGroups;
    static {
        Gson gson = new Gson();
        Type freeplayGroupListType = new TypeToken<List<FreeplayGroup>>() {}.getType();

        try (FileReader reader = new FileReader("cleanedFreeplayGroups.json")) {
            freeplayGroups = gson.fromJson(reader, freeplayGroupListType);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static boolean hasFBads(int seed, int startRound, int endRound) {
        for (int round = startRound; round < endRound; ++round) {
            SeededRandom random = new SeededRandom(round + seed);
            float budget;
            if (round > 1) {
                budget = (float) (calculateBudget(round) * (1.5 - random.getNext()));
            } else {
                budget = calculateBudget(round);
            }

            List<Integer> testGroups = IntStream.range(0, 529).boxed().collect(Collectors.toList());
            shuffleSeeded(testGroups, round + seed);


            for (int index : testGroups) {
                FreeplayGroup freeplayGroup = freeplayGroups.get(index);

                if (freeplayGroup.score > budget) continue;
                boolean inBounds = false;
                for (FreeplayGroup.Bounds bounds : freeplayGroup.bounds) {
                    if (bounds.lowerBounds <= round && round <= bounds.upperBounds) {
                        inBounds = true;
                        break;
                    }
                }
                if (!inBounds) {
                    continue;
                }

                if (freeplayGroup.group.bloon.equals("BadFortified")) {
                    return true;
                }
                budget -= (float) freeplayGroup.score;
            }
            ++round;
        }
        return false;
    }

    public static void processSeedRange(int startSeed, int endSeed, int startRound, int endRound) {
        for (int seed = startSeed; seed < endSeed; ++seed) {
            if (!hasFBads(seed, startRound, endRound)) {
                System.out.println("Seed with no BadFortified: " + seed);
            }
        }
    }

    private static final int THREAD_COUNT = 100;  // Adjust thread count based on available cores
    public static void main(String[] args) {
        final int totalSeeds = 1000000;
        final int seedsPerThread = 1000;
        final int startRound = 201;
        final int endRound = 250;

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        // Submit tasks to executor service
        List<Future<?>> futures = new ArrayList<>();
        for (int seed = 1; seed < totalSeeds; seed += seedsPerThread) {
            int startSeed = seed;
            int endSeed = Math.min(seed + seedsPerThread, totalSeeds);
            futures.add(executor.submit(() -> processSeedRange(startSeed, endSeed, startRound, endRound)));
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();  // Wait for each task to finish
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Shutdown executor service
        executor.shutdown();
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
}
