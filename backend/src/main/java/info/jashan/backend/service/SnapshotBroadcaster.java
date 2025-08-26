package info.jashan.backend.service;

import info.jashan.backend.model.Snapshot;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

public class SnapshotBroadcaster {
    private final RiskEngine engine;

    public SnapshotBroadcaster(RiskEngine engine) {
        this.engine = engine;
    }

    public Flux<Snapshot> flux() { return engine.snapshots(); }
}