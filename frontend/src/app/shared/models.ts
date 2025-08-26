export interface PerSymbol { qty: number; avgPrice: number; mid: number; unrealizedPnL: number; exposure: number; }
export interface Totals { netExposure: number; grossExposure: number; realizedPnL: number; unrealizedPnL: number; totalPnL: number; }
export interface Limits { grossExposureLimit: number; singleNameLimit: number; drawdownLimit: number; }
export interface Health { marketDataFeed: boolean; db: boolean; riskEngine: boolean; }
export interface Alert { type: string; message: string; ts: number; }
export interface Snapshot {
  ts: number;
  perSymbol: Record<string, PerSymbol>;
  totals: Totals;
  limits: Limits;
  health: Health;
  tradesPerSec: number;
  status: 'OK' | 'ALERT';
  alerts: Alert[];
}