package info.jashan.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import info.jashan.backend.service.*;
import info.jashan.backend.model.Trade;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class RiskDashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiskDashboardApplication.class, args);
    }

    @Bean
    public TradeGenerator tradeGenerator() {
        return new TradeGenerator();
    }

    @Bean
    public RiskEngine riskEngine(TradeGenerator gen) {
        return new RiskEngine(gen.flux(), gen);
    }

    @Bean
    public SnapshotBroadcaster broadcaster(RiskEngine engine) {
        return new SnapshotBroadcaster(engine);
    }
}

