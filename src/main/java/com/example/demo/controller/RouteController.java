package com.example.demo.controller;

import com.example.demo.service.routing.RouteOptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/route")
public class RouteController {

    private final RouteOptimizationService optimizer;

    public RouteController(RouteOptimizationService optimizer) {
        this.optimizer = optimizer;
    }

    @PostMapping("/optimize")
    public ResponseEntity<String> optimize(@RequestBody String req) {
        System.out.println("Optimize request: " + req);
        //RouteSolution sol = optimizer.optimizeRoute(req.getContainers(), req.getVehicle());
        //return ResponseEntity.ok(sol);
        return ResponseEntity.ok("ok");
    }


}
