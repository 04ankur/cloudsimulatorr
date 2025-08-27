import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;

/**
 * To run this, you'll need to set up a Java project with Maven or Gradle
 * and add the following dependencies:
 * 1. Javalin: A simple web framework for Java/Kotlin
 * - Maven: <dependency><groupId>io.javalin</groupId><artifactId>javalin</artifactId><version>5.6.3</version></dependency>
 * 2. Jackson: A library for handling JSON data
 * - Maven: <dependency><groupId>com.fasterxml.jackson.core</groupId><artifactId>jackson-databind</artifactId><version>2.15.2</version></dependency>
 * 3. SLF4J Simple: For logging
 * - Maven: <dependency><groupId>org.slf4j</groupId><artifactId>slf4j-simple</artifactId><version>2.0.7</version></dependency>
 */
public class CloudSimBackend {

    // ObjectMapper is used to convert Java objects to/from JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random random = new Random();

    public static void main(String[] args) {
        // Start the Javalin server on port 7070
        Javalin app = Javalin.create(config -> {
            // Enable CORS (Cross-Origin Resource Sharing) to allow requests from the browser
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
            // *** ADD THIS LINE ***
            // This tells Javalin to serve static files (like index.html) from the 'static' resources folder.
            config.staticFiles.add("/static");
        }).start(7070);

        System.out.println("Backend server started on http://localhost:7070");

        // Define the API endpoint for running the simulation
        // It listens for POST requests on the "/run-simulation" path
        app.post("/run-simulation", ctx -> {
            try {
                // 1. Get the configuration JSON from the frontend request
                String requestBody = ctx.body();
                SimulationConfig config = objectMapper.readValue(requestBody, SimulationConfig.class);

                System.out.println("Received simulation request: " + config.vmCount + " VMs, " + config.cloudletCount + " Cloudlets.");

                // 2. *** RUN THE SIMULATION ***
                // This is where you would integrate the actual CloudSim library.
                SimulationResult result = runMockSimulation(config);

                // 3. Send the results back to the frontend as JSON
                ctx.json(result);
                System.out.println("Simulation finished. Sent results back to client.");

            } catch (Exception e) {
                e.printStackTrace();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error processing simulation request.");
                errorResponse.put("message", e.getMessage());
                ctx.status(500).json(errorResponse);
            }
        });
    }

    /**
     * A mock simulation engine. This replaces a full CloudSim implementation for this example.
     */
    private static SimulationResult runMockSimulation(SimulationConfig config) {
        // Simulate VM allocation
        List<Map<String, Object>> hostLayout = new ArrayList<>();
        int vmsToAllocate = config.vmCount;
        for (int i = 0; i < config.hostCount; i++) {
            Map<String, Object> host = new HashMap<>();
            host.put("id", i);
            List<Integer> allocatedVmIds = new ArrayList<>();
            int vmsOnThisHost = 0;
            if (vmsToAllocate > 0) {
                 vmsOnThisHost = Math.min(vmsToAllocate, 2 + random.nextInt(2));
            }
           
            for(int j = 0; j < vmsOnThisHost; j++) {
                allocatedVmIds.add((config.vmCount - vmsToAllocate) + 1);
                vmsToAllocate--;
            }
            host.put("vms", allocatedVmIds);
            hostLayout.add(host);
        }

        // Simulate execution time
        double baseTimePerCloudlet = (double) config.cloudletLength / (config.mipsPerPe * config.pesPerVm);
        double totalTime = (baseTimePerCloudlet * config.cloudletCount) / config.vmCount; // Highly simplified
        totalTime *= (1.1 + random.nextDouble() * 0.2); // Add some randomness

        // Simulate results
        List<CloudletResult> cloudletResults = new ArrayList<>();
        for (int i = 0; i < config.cloudletCount; i++) {
            double startTime = random.nextDouble() * totalTime * 0.5;
            double executionTime = baseTimePerCloudlet * (0.9 + random.nextDouble() * 0.2);
            cloudletResults.add(new CloudletResult(i, startTime, startTime + executionTime));
        }
        
        double successRate = 95.0 + random.nextDouble() * 5.0;
        double ramUtilization = 60.0 + random.nextDouble() * 25.0;

        return new SimulationResult(totalTime, successRate, ramUtilization, hostLayout, cloudletResults);
    }

    // --- Data Classes for JSON mapping ---

    static class SimulationConfig {
        public int hostCount;
        public int pesPerHost;
        public int ramPerHost;
        public int mipsPerPe;
        public int vmCount;
        public int pesPerVm;
        public int ramPerVm;
        public int cloudletCount;
        public int cloudletLength;
    }

    static class CloudletResult {
        public int id;
        public double startTime;
        public double finishTime;

        public CloudletResult() {}

        public CloudletResult(int id, double startTime, double finishTime) {
            this.id = id;
            this.startTime = startTime;
            this.finishTime = finishTime;
        }
    }

    static class SimulationResult {
        public double totalTime;
        public double successRate;
        public double ramUtilization;
        public List<Map<String, Object>> hostLayout;
        public List<CloudletResult> cloudletResults;

        public SimulationResult() {}

        public SimulationResult(double totalTime, double successRate, double ramUtilization, List<Map<String, Object>> hostLayout, List<CloudletResult> cloudletResults) {
            this.totalTime = totalTime;
            this.successRate = successRate;
            this.ramUtilization = ramUtilization;
            this.hostLayout = hostLayout;
            this.cloudletResults = cloudletResults;
        }
    }
}
