
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RaceConditionReproduction {

    static class UsageStats {
        int totalTokens = 0;

        static UsageStats fromJson(String json) {
            UsageStats s = new UsageStats();
            if (json != null && !json.isEmpty()) {
                s.totalTokens = Integer.parseInt(json);
            }
            return s;
        }

        String toJson() {
            return String.valueOf(totalTokens);
        }
    }

    static class PreferencesManager {
        private String sharedPrefsValue = "0"; // Simulating file storage

        // Simulate getUsageStats
        UsageStats getUsageStats() {
            // Read from "disk"
            String json = sharedPrefsValue;
            return UsageStats.fromJson(json);
        }

        // Simulate saveUsageStats
        void saveUsageStats(UsageStats stats) {
            // Write to "disk"
            sharedPrefsValue = stats.toJson();
        }

        // VULNERABLE METHOD
        void recordApiSuccess(int tokens) {
            UsageStats stats = getUsageStats();

            // Simulate processing time/race window
            try { Thread.sleep(50); } catch (InterruptedException e) {}

            stats.totalTokens += tokens;

            saveUsageStats(stats);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final PreferencesManager prefs = new PreferencesManager();
        int threadCount = 10;
        int incrementsPerThread = 1;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        System.out.println("Starting race condition simulation...");
        System.out.println("Expected Total: " + (threadCount * 100)); // Each adds 100

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                prefs.recordApiSuccess(100);
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        int finalTokens = prefs.getUsageStats().totalTokens;
        System.out.println("Actual Total: " + finalTokens);

        if (finalTokens != threadCount * 100) {
            System.out.println("FAIL: Race condition detected! Data was lost.");
            System.exit(1);
        } else {
            System.out.println("SUCCESS: No race condition detected.");
        }
    }
}
