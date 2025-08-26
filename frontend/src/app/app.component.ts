import { Component, OnDestroy, OnInit } from '@angular/core';
import { RiskService } from './core/risk.service';
import { Snapshot, Limits } from './shared/models';
import { ControlsComponent } from './features/controls/controls.component';
import { HealthComponent } from './features/health/health.component';
import { PnlChartComponent } from './features/pnl-chart/pnl-chart.component';
import { TilesComponent } from './features/tiles/tiles.component';
import { TopExposuresComponent } from './features/top-exposures/top-exposures.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [ControlsComponent, HealthComponent, PnlChartComponent, TilesComponent, TopExposuresComponent]
})
export class AppComponent implements OnInit, OnDestroy {
  snapshot: Snapshot | null = null;
  now = Date.now();
  private timer?: any;

  constructor(private risk: RiskService) { }

  ngOnInit() {
    this.risk.connect();
    this.risk.snapshot$.subscribe(s => {
      this.snapshot = s
    });
    this.timer = setInterval(() => this.now = Date.now(), 200);
  }
  ngOnDestroy() { clearInterval(this.timer); this.risk.disconnect(); }

  onAction(mode: 'VOLATILITY' | 'BURST' | 'FAIL_FEED' | 'RECOVER') { this.risk.stress(mode); }
  onUpdateLimits(l: Limits) { this.risk.setLimits(l); }
}