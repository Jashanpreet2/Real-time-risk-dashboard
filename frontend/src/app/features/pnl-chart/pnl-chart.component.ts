import { Component, Input, OnInit } from '@angular/core';
import { ChartData, ChartOptions } from 'chart.js';
import { Snapshot } from '../../shared/models';
import {NgChartsModule} from 'ng2-charts';

@Component({ selector: 'app-pnl-chart', templateUrl: './pnl-chart.component.html', imports: [NgChartsModule]})
export class PnlChartComponent implements OnInit {
  @Input() snapshot!: Snapshot;
  data: ChartData<'line'> = { labels: [], datasets: [{ data: [], label: 'Unrealized PnL' }] };
  options: ChartOptions<'line'> = { animation:false, responsive:true, scales:{ x:{display:false}, y:{ display:true } } };

  private history: { t:number, v:number }[] = [];

  ngOnInit(){}

  ngOnChanges(){
    if (!this.snapshot) return;
    this.history.push({ t: this.snapshot.ts, v: this.snapshot.totals.unrealizedPnL });
    if (this.history.length>120) this.history.shift();
    this.data = {
      labels: this.history.map(h=>new Date(h.t).toLocaleTimeString()),
      datasets: [{ data: this.history.map(h=>h.v), label: 'Unrealized PnL' }]
    };
  }
}