import { Component, Input } from '@angular/core';
import { Health } from '../../shared/models';

@Component({ selector: 'app-health', templateUrl: './health.component.html', styleUrls: ['./health.component.scss'] })
export class HealthComponent {
  @Input() health!: Health;
}