import { Component, EventEmitter, Output } from '@angular/core';
import { Limits } from '../../shared/models';
import { FormsModule } from '@angular/forms';

@Component({ selector: 'app-controls', templateUrl: './controls.component.html', styleUrls: ['./controls.component.scss'], imports: [FormsModule]})
export class ControlsComponent {
  @Output() action = new EventEmitter<'VOLATILITY' | 'BURST' | 'FAIL_FEED' | 'RECOVER'>();
  @Output() updateLimits = new EventEmitter<Limits>();

  limits: Limits = { grossExposureLimit: 5_000_000, singleNameLimit: 1_500_000, drawdownLimit: 250_000 };
}