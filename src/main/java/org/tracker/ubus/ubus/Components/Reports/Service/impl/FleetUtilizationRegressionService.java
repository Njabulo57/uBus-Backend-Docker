package org.tracker.ubus.ubus.Components.Reports.Service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Reports.DTO.Request.DailyRouteUtilizationDTO;
import org.tracker.ubus.ubus.Components.Reports.DTO.Request.ModelMetrics;
import org.tracker.ubus.ubus.Components.Reports.DTO.Request.UtilizationPrediction;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripReportsRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RestController
@RequiredArgsConstructor
public class FleetUtilizationRegressionService {

    private final TripReportsRepository tripReportsRepository;

    private OLSMultipleLinearRegression regressionModel;
    private double[] coefficients;
    private double rSquared;
    private double adjustedRSquared;
    private double mse;

    private double[] featureMeans;
    private double[] featureStdDevs;

    private static final String[] FEATURE_NAMES = {
            "Passengers",
            "Scheduled Trips",
            "Available Buses"
    };

    @PostConstruct
    public void trainModel() {
        log.info("Starting fleet utilization regression model training...");
        debugData();
        trainLinearRegression();

        logAllRouteUtilization();
        log.info("Model training completed successfully!");
    }

    public void trainLinearRegression() {
        var historicalData = collectHistoricalData();

        if (historicalData.isEmpty()) {
            log.warn("No historical data available for training!");
            return;
        }

        if (historicalData.size() < 4) {
            log.warn("Not enough data points! Need at least 4 records, but got {}", historicalData.size());
            log.warn("Skipping model training until more data is available.");
            return;
        }

        log.info("Collected {} historical records for training", historicalData.size());

        int numSamples = historicalData.size();
        int numFeatures = 3;

        double[][] X = new double[numSamples][numFeatures];
        double[] y = new double[numSamples];

        Random random = new Random();
        for (int i = 0; i < numSamples; i++) {
            DailyRouteUtilizationDTO record = historicalData.get(i);
            // Add tiny random noise (0.0001%) to break perfect multicollinearity
            double noise1 = 1 + (random.nextDouble() - 0.5) * 0.0001;
            double noise2 = 1 + (random.nextDouble() - 0.5) * 0.0001;
            double noise3 = 1 + (random.nextDouble() - 0.5) * 0.0001;

            X[i][0] = record.passengers().doubleValue() * noise1;
            X[i][1] = record.scheduledTrips().doubleValue() * noise2;
            X[i][2] = record.availableBuses().doubleValue() * noise3;

            y[i] = record.utilization();
        }

        standardizeFeatures(X);

        regressionModel = new OLSMultipleLinearRegression();
        regressionModel.newSampleData(y, X);

        coefficients = regressionModel.estimateRegressionParameters();
        rSquared = regressionModel.calculateRSquared();
        adjustedRSquared = regressionModel.calculateAdjustedRSquared();

        double[] residuals = regressionModel.estimateResiduals();
        mse = 0;
        for (double residual : residuals) {
            mse += residual * residual;
        }
        mse = mse / residuals.length;
        logFeatureImportance();
    }

    public void debugData() {
        var historicalData = collectHistoricalData();

        log.info("=== DATA DEBUG ===");
        log.info("Total records: {}", historicalData.size());

        if (historicalData.isEmpty()) {
            log.warn("No data!");
            return;
        }

        // Check if Passengers is always proportional to ScheduledTrips
        if (historicalData.size() > 1) {
            DailyRouteUtilizationDTO first = historicalData.get(0);
            double firstRatio = (double) first.passengers() / first.scheduledTrips();
            boolean allSameRatio = historicalData.stream()
                    .allMatch(dto -> Math.abs((double) dto.passengers() / dto.scheduledTrips() - firstRatio) < 0.0001);

            if (allSameRatio) {
                log.warn("WARNING: Passengers is always exactly {} * ScheduledTrips!", firstRatio);
                log.warn("These features are perfectly correlated - use only one of them!");
            }
        }
    }

    public void logAllRouteUtilization() {
        log.info("=== FLEET UTILIZATION FOR ALL ROUTES (ALL DATES) ===");

        List<DailyRouteUtilizationDTO> allData = collectHistoricalData();

        if (allData.isEmpty()) {
            log.warn("No utilization data available!");
            return;
        }

        // Group by route
        Map<String, List<DailyRouteUtilizationDTO>> dataByRoute = allData.stream()
                .collect(Collectors.groupingBy(DailyRouteUtilizationDTO::routeName));

        // Log summary per route
        for (Map.Entry<String, List<DailyRouteUtilizationDTO>> entry : dataByRoute.entrySet()) {
            String routeName = entry.getKey();
            List<DailyRouteUtilizationDTO> routeData = entry.getValue();

            // Calculate statistics for this route
            DoubleSummaryStatistics stats = routeData.stream()
                    .mapToDouble(DailyRouteUtilizationDTO::utilization)
                    .summaryStatistics();

            double avgUtilization = stats.getAverage();
            double maxUtilization = stats.getMax();
            double minUtilization = stats.getMin();
            long totalDays = routeData.size();

            // Count critical days (>85% utilization)
            long criticalDays = routeData.stream()
                    .filter(dto -> dto.utilization() > 85)
                    .count();

            // Count low utilization days (<30%)
            long lowUtilizationDays = routeData.stream()
                    .filter(dto -> dto.utilization() < 30)
                    .count();

        }
        log.info("=============================================");
    }

    private void standardizeFeatures(double[][] X) {
        int numFeatures = X[0].length;
        int numSamples = X.length;

        featureMeans = new double[numFeatures];
        featureStdDevs = new double[numFeatures];

        for (int j = 0; j < numFeatures; j++) {
            double sum = 0;
            for (int i = 0; i < numSamples; i++) {
                sum += X[i][j];
            }
            featureMeans[j] = sum / numSamples;
        }

        for (int j = 0; j < numFeatures; j++) {
            double sumSquaredDiff = 0;
            for (int i = 0; i < numSamples; i++) {
                double diff = X[i][j] - featureMeans[j];
                sumSquaredDiff += diff * diff;
            }
            featureStdDevs[j] = Math.sqrt(sumSquaredDiff / numSamples);

            if (featureStdDevs[j] > 0) {
                for (int i = 0; i < numSamples; i++) {
                    X[i][j] = (X[i][j] - featureMeans[j]) / featureStdDevs[j];
                }
            }
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        return Math.round(value * Math.pow(10, places)) / Math.pow(10, places);
    }

    private List<DailyRouteUtilizationDTO> collectHistoricalData() {
        // Query 1: Passengers per route per day
        List<Object[]> passengerData = tripReportsRepository.findPassengersPerRoutePerDay();

        // Query 2: Scheduled trips per route per day
        List<Object[]> scheduledData = tripReportsRepository.findDailyScheduledTripsByRoute();

        // Query 3: Buses used per route per day
        List<Object[]> busesUsedData = tripReportsRepository.findDailyBusesUsedByRoute();

        // Query 4: Available buses per day - NOW ROUTE SPECIFIC
        List<Object[]> availableBusesData = tripReportsRepository.findDailyAvailableBusesByRoute(
                BusOperationalStatus.OPERATIONAL
        );

        Map<String, Long> passengerMap = new HashMap<>();
        Map<String, Long> scheduledMap = new HashMap<>();
        Map<String, Long> busesUsedMap = new HashMap<>();
        Map<String, Long> availableBusesMap = new HashMap<>(); // Changed to String key (date_route)

        // Build passenger map
        for (Object[] row : passengerData) {
            LocalDate date = (LocalDate) row[0];
            Route route = (Route) row[1];
            Long count = (Long) row[2];
            String key = date.toString() + "]" + route.name();

            passengerMap.put(key, count);
        }

        // Build scheduled trips map
        for (Object[] row : scheduledData) {
            LocalDate date = (LocalDate) row[0];
            Route route = (Route) row[1];
            Long count = (Long) row[2];
            String key = date.toString() + "]" + route.name();

            scheduledMap.put(key, count);
        }

        // Build buses used map
        for (Object[] row : busesUsedData) {
            LocalDate date = (LocalDate) row[0];
            Route route = (Route) row[1];
            Long count = (Long) row[2];
            String key = date.toString() + "]" + route.name();
            busesUsedMap.put(key, count);
        }

        // Build available buses map - NEW: route-specific
        for (Object[] row : availableBusesData) {
            LocalDate date = (LocalDate) row[0];
            Route route = (Route) row[1];
            Long count = (Long) row[2];
            String key = date.toString() + "]" + route.name();
            availableBusesMap.put(key, count);

        }

        List<DailyRouteUtilizationDTO> result = new ArrayList<>();

        for (Map.Entry<String, Long> entry : busesUsedMap.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("]");
            LocalDate date = LocalDate.parse(parts[0]);
            String routeName = parts[1];

            Long passengers = passengerMap.getOrDefault(key, 0L);
            Long scheduledTrips = scheduledMap.getOrDefault(key, 0L);
            Long busesUsed = entry.getValue();

            // Get route-specific available buses, default to 1 if not found
            Long availableBuses = availableBusesMap.getOrDefault(key, 1L);

            // FIX: Cap utilization at 100% to prevent values > 100%
            double utilization = Math.min((double) busesUsed / availableBuses * 100, 100);

            DailyRouteUtilizationDTO dto = DailyRouteUtilizationDTO.builder()
                    .date(date)
                    .routeId(null)
                    .routeName(routeName)
                    .passengers(passengers)
                    .scheduledTrips(scheduledTrips)
                    .busesUsed(busesUsed)
                    .availableBuses(availableBuses)
                    .utilization(utilization)
                    .build();

            result.add(dto);
        }

        result.sort(Comparator.comparing(DailyRouteUtilizationDTO::date));

        return result;
    }

    public UtilizationPrediction predictUtilization(
            Long passengers,
            Long scheduledTrips,
            Long availableBuses) {

        if (regressionModel == null || coefficients == null) {
            throw new IllegalStateException("Model not trained yet!");
        }

        double[] features = new double[3];
        features[0] = passengers.doubleValue();
        features[1] = scheduledTrips.doubleValue();
        features[2] = availableBuses.doubleValue();

        for (int i = 0; i < features.length; i++) {
            if (featureStdDevs[i] > 0) {
                features[i] = (features[i] - featureMeans[i]) / featureStdDevs[i];
            }
        }

        double predictedUtilization = coefficients[0];
        for (int i = 0; i < features.length; i++) {
            predictedUtilization += coefficients[i + 1] * features[i];
        }

        predictedUtilization = Math.clamp(predictedUtilization, 0, 100);

        return UtilizationPrediction.builder()
                .predictedUtilization(predictedUtilization)
                .passengers(passengers)
                .scheduledTrips(scheduledTrips)
                .availableBuses(availableBuses)
                .modelRSquared(rSquared)
                .build();
    }

    public List<UtilizationPrediction> predictAllRoutes(LocalDate targetDate) {
        List<DailyRouteUtilizationDTO> currentData = collectHistoricalData();

        return currentData.stream()
                .filter(dto -> dto.date().equals(targetDate))
                .map(dto -> predictUtilization(
                        dto.passengers(),
                        dto.scheduledTrips(),
                        dto.availableBuses()
                ))
                .collect(Collectors.toList());
    }

    private void logFeatureImportance() {
        if (coefficients == null || coefficients.length < 2) {
            return;
        }

        log.info("=== Feature Importance ===");
        for (int i = 0; i < Math.min(FEATURE_NAMES.length, coefficients.length - 1); i++) {
            log.info("{}: {:.4f}", FEATURE_NAMES[i], coefficients[i + 1]);
        }
        log.info("Intercept: {:.4f}", coefficients[0]);
        log.info("==========================");

        double absSum = 0;
        for (int i = 0; i < Math.min(FEATURE_NAMES.length, coefficients.length - 1); i++) {
            absSum += Math.abs(coefficients[i + 1]);
        }

        if (absSum > 0) {
            log.info("=== Relative Importance ===");
            for (int i = 0; i < Math.min(FEATURE_NAMES.length, coefficients.length - 1); i++) {
                double importance = Math.abs(coefficients[i + 1]) / absSum * 100;
                log.info("{}: {:.1f}%", FEATURE_NAMES[i], importance);
            }
            log.info("============================");
        }
    }

    public ModelMetrics getModelMetrics() {
        if (regressionModel == null) {
            return null;
        }

        Map<String, Double> featureImportance = new LinkedHashMap<>();
        if (coefficients != null && coefficients.length > 1) {
            double absSum = 0;
            for (int i = 0; i < Math.min(FEATURE_NAMES.length, coefficients.length - 1); i++) {
                absSum += Math.abs(coefficients[i + 1]);
            }

            if (absSum > 0) {
                for (int i = 0; i < Math.min(FEATURE_NAMES.length, coefficients.length - 1); i++) {
                    double importance = Math.abs(coefficients[i + 1]) / absSum * 100;
                    featureImportance.put(FEATURE_NAMES[i], importance);
                }
            }
        }

        return ModelMetrics.builder()
                .rSquared(rSquared)
                .adjustedRSquared(adjustedRSquared)
                .mse(mse)
                .featureImportance(featureImportance)
                .build();
    }
}