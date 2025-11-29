package com.example.demo.service.routing;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class OSRMRoutingService {

    private final RestTemplate rest;
    private final String osrmBase;

    public OSRMRoutingService(RestTemplate rest, @Value("${osrm.url}") String osrmBase) {
        this.rest = rest;
        this.osrmBase = osrmBase;
    }

    // Build duration matrix using /table?annotations=duration
    // points: list of double[]{lat, lng}
    public double[][] getDurationMatrix(List<double[]> points) {
        // OSRM expects lon,lat pairs separated by ';' and within each pair lon,lat
        String coords = points.stream()
                .map(p -> String.format(Locale.ROOT, "%f,%f", p[1], p[0])) // lon,lat
                .collect(Collectors.joining(";"));

        String url = String.format("%s/table/v1/driving/%s?annotations=duration", osrmBase, coords);
        Map<?,?> resp = rest.getForObject(url, Map.class);
        // parse durations (list of lists)
        List<List<Number>> durations = (List<List<Number>>) resp.get("durations"); // seconds as Numbers
        int n = durations.size();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            List<Number> row = durations.get(i);
            for (int j = 0; j < n; j++) {
                Number val = row.get(j);
                matrix[i][j] = val == null ? Double.POSITIVE_INFINITY : val.doubleValue(); // seconds
            }
        }
        return matrix;
    }

    // Get route overview polyline and total distance/duration for the ordered list of points
    // orderedPoints includes truck start first then containers in chosen order
    public RouteResult getRouteForOrderedPoints(List<double[]> orderedPoints) {
        String coords = orderedPoints.stream()
                .map(p -> String.format(Locale.ROOT, "%f,%f", p[1], p[0])) // lon,lat
                .collect(Collectors.joining(";"));

        // request steps + overview polyline + annotations
        String url = String.format("%s/route/v1/driving/%s?overview=full&geometries=polyline&steps=false", osrmBase, coords);
        Map<?,?> resp = rest.getForObject(url, Map.class);

        List<Map<String,Object>> routes = (List<Map<String,Object>>) resp.get("routes");
        if (routes == null || routes.isEmpty()) {
            return new RouteResult("", 0.0, 0.0);
        }
        Map<String,Object> route = routes.get(0);
        // distance in meters, duration in seconds
        double distanceMeters = ((Number)route.get("distance")).doubleValue();
        double durationSeconds = ((Number)route.get("duration")).doubleValue();
        String polyline = (String) route.get("geometry"); // encoded polyline

        return new RouteResult(polyline, distanceMeters / 1000.0, durationSeconds / 60.0); // km and minutes
    }

    // simple holder
    public static class RouteResult {
        public final String encodedPolyline;
        public final double distanceKm;
        public final double durationMin;
        public RouteResult(String encodedPolyline, double distanceKm, double durationMin){
            this.encodedPolyline = encodedPolyline;
            this.distanceKm = distanceKm;
            this.durationMin = durationMin;
        }
    }
}
