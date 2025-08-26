package info.jashan.backend.web;

import info.jashan.backend.model.Health;
import info.jashan.backend.service.RiskEngine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final RiskEngine engine;

    public HealthController(RiskEngine engine) {
        this.engine = engine;
    }

    @GetMapping("/api/health")
    public Health health() {
        return engine.snapshots().blockFirst().health();
    }
}