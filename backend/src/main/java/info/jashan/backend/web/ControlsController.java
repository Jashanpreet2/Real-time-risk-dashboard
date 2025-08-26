package info.jashan.backend.web;

import info.jashan.backend.service.RiskEngine;
import info.jashan.backend.service.TradeGenerator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/controls")
@CrossOrigin(origins = "http://localhost:4200")
public class ControlsController {
    private final RiskEngine engine;
    private final TradeGenerator generator;

    public ControlsController(RiskEngine engine, TradeGenerator generator) {
        this.engine = engine; this.generator = generator; }

    @PostMapping("/stress")
    public void stress(@RequestParam("mode") String mode) {
        System.out.println("Stress mode: " + mode);
        switch (mode) {
            case "VOLATILITY" -> engine.setMode(TradeGenerator.Mode.VOLATILITY, generator);
            case "BURST" -> engine.setMode(TradeGenerator.Mode.BURST, generator);
            case "FAIL_FEED" -> engine.setMode(TradeGenerator.Mode.FEED_DOWN, generator);
            case "RECOVER" -> engine.recover(generator);
            default -> {}
        }
    }
}