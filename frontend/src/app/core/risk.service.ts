import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Snapshot, Limits } from '../shared/models';

@Injectable({ providedIn: 'root' })
export class RiskService {
  snapshot$ = new BehaviorSubject<Snapshot | null>(null);
  private es?: EventSource;
  private readonly base = 'http://localhost:8080';

  constructor(private zone: NgZone) { }

  connect() {
    if (this.es) return;
    this.es = new EventSource(`${this.base}/sse/risk`);
    this.es.onmessage = (e) => {
      const data: Snapshot = JSON.parse(e.data);
      this.zone.run(() => this.snapshot$.next(data));
    };
    this.es.onerror = () => {
      // attempt reconnect
      this.disconnect();
      setTimeout(() => this.connect(), 1500);
    };
  }

  disconnect(){ this.es?.close(); this.es = undefined; }

  stress(mode: 'VOLATILITY'|'BURST'|'FAIL_FEED'|'RECOVER'){
    return fetch(`${this.base}/api/controls/stress?mode=${mode}`, { method: 'POST' });
  }

  getLimits(){ return fetch(`${this.base}/api/limits`).then(r=>r.json() as Promise<Limits>); }
  setLimits(l: Limits){ return fetch(`${this.base}/api/limits`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(l)}); }
}