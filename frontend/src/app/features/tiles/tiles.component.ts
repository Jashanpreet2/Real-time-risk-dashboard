import { Component, Input } from '@angular/core';
import { Totals, Limits, Alert } from '../../shared/models';

@Component({ selector: 'app-tiles', templateUrl: './tiles.component.html', styleUrls: ['./tiles.component.scss'] })
export class TilesComponent {
  @Input() totals!: Totals;
  @Input() limits!: Limits;
  @Input() alerts: Alert[] = [];

  fmt(v:number){
    const abs = Math.abs(v);
    if (Number.isNaN(abs)) {
        return null
    }
    if (abs >= 1_000_000) return (v/1_000_000).toFixed(2)+'M';
    if (abs >= 1_000) return (v/1_000).toFixed(2)+'K';
    return v.toFixed(0);
  }
}