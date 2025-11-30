package com.example.demo.service.routing;

import com.example.demo.dto.RouteSolution;
import com.example.demo.models.Container;
import com.example.demo.models.Vehicle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteOptimizationService {

    private final OSRMRoutingService osrm;

    public RouteOptimizationService(OSRMRoutingService osrm) {
        this.osrm = osrm;
    }

    public RouteSolution optimizeRoute(List<Container> containers, Vehicle truck) {

        if (containers == null || containers.isEmpty()) {
            return new RouteSolution(Collections.emptyList(), 0, 0, "");
        }

        // Build points array: index 0 is truck, then containers
        List<double[]> allPoints = new ArrayList<>();
        allPoints.add(truck.getLocation());
        containers.forEach(c -> allPoints.add(c.getLocation()));

        // 1) Get duration matrix (in seconds)
        double[][] durationMatrix = osrm.getDurationMatrix(allPoints);

        // 2) Solve TSP on matrix (we use durations as metric)
        List<Integer> order = solveTspNearestNeighborPlus2Opt(durationMatrix);

        // order is indices into allPoints (0..n-1) starting with 0 (truck)
        // Convert to container order (exclude 0)
        List<String> orderedContainerIds = order.stream()
                .filter(i -> i != 0)
                .map(i -> containers.get(i - 1).getId())
                .collect(Collectors.toList());

        // 3) Request the actual route polyline & distance/duration from OSRM using the ordered points
        List<double[]> orderedPointsForRouting = order.stream()
                .map(allPoints::get)
                .collect(Collectors.toList());

        OSRMRoutingService.RouteResult routeResult = osrm.getRouteForOrderedPoints(orderedPointsForRouting);

        // 4) return solution
        return new RouteSolution(orderedContainerIds, routeResult.distanceKm, routeResult.durationMin, routeResult.encodedPolyline);
    }

    // --------- TSP heuristic: Nearest Neighbor + 2-opt ---------

    private List<Integer> solveTspNearestNeighborPlus2Opt(double[][] matrix) {
        int n = matrix.length;
        // Nearest Neighbor from 0
        boolean[] used = new boolean[n];
        List<Integer> tour = new ArrayList<>();
        int current = 0;
        tour.add(current);
        used[current] = true;
        for (int step = 1; step < n; step++) {
            int next = -1;
            double best = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (!used[j] && matrix[current][j] < best) {
                    best = matrix[current][j];
                    next = j;
                }
            }
            if (next == -1) break;
            used[next] = true;
            tour.add(next);
            current = next;
        }
        // 2-opt improvement (on the tour excluding returning to 0)
        tour = twoOptImprove(tour, matrix);
        return tour;
    }

    private List<Integer> twoOptImprove(List<Integer> tour, double[][] matrix) {
        boolean improved = true;
        int n = tour.size();
        while (improved) {
            improved = false;
            for (int i = 1; i < n - 2; i++) {
                for (int k = i + 1; k < n - 0; k++) {
                    double delta = -matrix[tour.get(i-1)][tour.get(i)]
                            -matrix[tour.get(k)][tour.get((k+1)%n)]
                            +matrix[tour.get(i-1)][tour.get(k)]
                            +matrix[tour.get(i)][tour.get((k+1)%n)];
                    if (delta < -1e-6) { // improvement
                        Collections.reverse(tour.subList(i, k+1));
                        improved = true;
                    }
                }
            }
        }
        return tour;
    }
}
