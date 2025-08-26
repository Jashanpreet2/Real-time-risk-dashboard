package info.jashan.backend.web;

import info.jashan.backend.model.Limits;
import info.jashan.backend.model.Snapshot;
import info.jashan.backend.service.RiskEngine;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class RiskRestController {
    private final RiskEngine engine;
    public RiskRestController(RiskEngine engine) { this.engine = engine; }

    @GetMapping("/risk/snapshot")
    public Mono<Snapshot> snapshot() { return engine.snapshots().next(); }

    @GetMapping("/limits")
    public Limits getLimits(){ return engine.getLimits(); }

    @PostMapping("/limits")
    public void setLimits(@RequestBody Limits l){ engine.setLimits(l); }
}