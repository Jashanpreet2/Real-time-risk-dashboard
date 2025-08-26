import { Component, Input } from '@angular/core';
import { PerSymbol } from '../../shared/models';
import { CommonModule } from '@angular/common';

@Component({ selector: 'app-top-exposures',
    templateUrl: './top-exposures.component.html', 
    styleUrls: ['./top-exposures.component.scss'],
    imports: [CommonModule]
})
export class TopExposuresComponent {
    @Input() perSymbol!: Record<string, PerSymbol>;
    @Input() limit!: number;

    symbols() {
        const ret = Object.entries(this.perSymbol)
            .map(([k, v]) => ({ sym: k, exp: v.exposure, qty: v.qty }))
            .sort((a, b) => b.exp - a.exp)
            .slice(0, 5);
        console.log(ret)
        return ret;
    }
}