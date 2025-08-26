package info.jashan.backend.web;

import info.jashan.backend.model.Snapshot;
import info.jashan.backend.service.SnapshotBroadcaster;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class RiskSseController {
    private final SnapshotBroadcaster broadcaster;
    public RiskSseController(SnapshotBroadcaster broadcaster) { this.broadcaster = broadcaster; }

    @CrossOrigin(origins="http://localhost:4200/")
    @GetMapping(path = "/sse/risk", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Snapshot> stream() {
        return broadcaster.flux();
    }
}