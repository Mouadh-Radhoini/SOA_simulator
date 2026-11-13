import 'zone.js';
import { bootstrapApplication } from '@angular/platform-browser';
import { HttpClient, HttpInterceptorFn, provideHttpClient, withInterceptors } from '@angular/common/http';
import { Component, inject, Injectable, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, EMPTY, Subject, catchError, takeUntil } from 'rxjs';

type DashboardConfig = {
  username?: string;
  password?: string;
  apiUsername?: string;
  apiPassword?: string;
};

declare global {
  interface Window { SOA_WS_CONFIG?: DashboardConfig; }
}

const config = (): DashboardConfig => window.SOA_WS_CONFIG ?? {};

export const dashboardAuthInterceptor: HttpInterceptorFn = (request, next) => {
  const { apiUsername, apiPassword } = config();
  if (!apiUsername || !apiPassword || request.headers.has('Authorization')) return next(request);
  const authorization = `Basic ${btoa(`${apiUsername}:${apiPassword}`)}`;
  return next(request.clone({ setHeaders: { Authorization: authorization } }));
};

type Borne = {
  borneId: string;
  containerName: string;
  containerStatus: string;
  applicationHealthStatus: string;
  consecutiveFailures: number;
  lastHealthCheck: string;
  mqttAvailability: string;
};

export type BorneEvent = {
  eventId: string;
  eventType: string;
  borneId: string;
  occurredAt: string;
  data: Record<string, unknown>;
};

function isBorneEvent(value: unknown): value is BorneEvent {
  if (!value || typeof value !== 'object') return false;
  const event = value as Partial<BorneEvent>;
  return typeof event.eventId === 'string'
    && typeof event.eventType === 'string'
    && typeof event.borneId === 'string'
    && typeof event.occurredAt === 'string'
    && !!event.data
    && typeof event.data === 'object';
}

@Injectable({ providedIn: 'root' })
export class WebsocketService implements OnDestroy {
  private readonly client = new Client({
    brokerURL: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`,
    connectHeaders: {
      login: config().username ?? '',
      passcode: config().password ?? ''
    },
    reconnectDelay: 2000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000
  });
  private readonly eventsSubject = new Subject<BorneEvent>();
  private readonly connectedSubject = new BehaviorSubject(false);
  readonly events$ = this.eventsSubject.asObservable();
  readonly connected$ = this.connectedSubject.asObservable();
  private subscriptions: StompSubscription[] = [];

  constructor() {
    this.client.onConnect = () => {
      this.connectedSubject.next(true);
      this.unsubscribeAll();
      ['/topic/bornes/status', '/topic/bornes/telemetry', '/topic/alerts'].forEach(destination => {
        this.subscriptions.push(this.client.subscribe(destination, (message: IMessage) => {
          try {
            const event: unknown = JSON.parse(message.body);
            if (isBorneEvent(event)) this.eventsSubject.next(event);
          } catch {
            // Ignore malformed broker messages without terminating realtime updates.
          }
        }));
      });
    };
    this.client.onWebSocketClose = () => this.connectedSubject.next(false);
    this.client.onWebSocketError = () => this.connectedSubject.next(false);
    this.client.onDisconnect = () => this.connectedSubject.next(false);
    this.client.activate();
  }

  private unsubscribeAll() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.subscriptions = [];
  }

  ngOnDestroy() {
    this.unsubscribeAll();
    this.client.deactivate();
    this.connectedSubject.complete();
    this.eventsSubject.complete();
  }
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  template: `
    <main>
      <h1>SOA Borne Dashboard</h1>
      <button (click)="load()" [disabled]="loading">Refresh</button>
      <span [class.success]="connected" [class.error]="!connected">
        Realtime: {{ connected ? 'connected' : 'disconnected' }}
      </span>
      <p *ngIf="error" class="error">{{ error }}</p>
      <section class="cards">
        <b class="card">Total {{ bornes.length }}</b>
        <b class="card">Healthy {{ healthy }}</b>
        <b class="card">Stopped {{ stopped }}</b>
      </section>
      <section *ngIf="alerts.length">
        <h2>Alerts</h2>
        <p *ngFor="let alert of alerts">{{ alert.eventType }} — {{ alert.borneId }}</p>
      </section>
      <table>
        <thead><tr><th>Borne</th><th>Container</th><th>Technical health</th><th>MQTT</th><th>Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let borne of bornes">
            <td>{{ borne.borneId }}</td><td>{{ borne.containerName }}</td>
            <td>{{ borne.applicationHealthStatus }}</td><td>{{ borne.mqttAvailability }}</td>
            <td>
              <button (click)="command(borne, 'start')">Start</button>
              <button (click)="command(borne, 'stop')">Stop</button>
              <button (click)="command(borne, 'restart')">Restart</button>
            </td>
          </tr>
          <tr *ngIf="!loading && !bornes.length"><td colspan="5">No Bornes registered.</td></tr>
        </tbody>
      </table>
    </main>
  `
})
export class AppComponent implements OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly realtime = inject(WebsocketService);
  private readonly destroyed = new Subject<void>();
  bornes: Borne[] = [];
  alerts: BorneEvent[] = [];
  connected = false;
  loading = false;
  error = '';

  get healthy() { return this.bornes.filter(b => b.applicationHealthStatus === 'HEALTHY').length; }
  get stopped() { return this.bornes.filter(b => b.applicationHealthStatus === 'STOPPED').length; }

  constructor() {
    this.load();
    this.realtime.connected$.pipe(takeUntil(this.destroyed)).subscribe(value => {
      this.connected = value;
      if (value) this.load();
    });
    this.realtime.events$.pipe(takeUntil(this.destroyed)).subscribe(event => {
      if (event.eventType === 'BORNE_ALERT_RAISED') this.alerts = [event, ...this.alerts].slice(0, 20);
      if (event.eventType.startsWith('BORNE_')) this.load();
    });
  }

  load() {
    if (this.loading) return;
    this.loading = true;
    this.error = '';
    this.http.get<Borne[]>('/api/management/bornes').pipe(
      catchError(() => {
        this.error = 'Unable to load Borne status. Check the API connection.';
        return EMPTY;
      })
    ).subscribe({
      next: value => this.bornes = value,
      complete: () => this.loading = false
    });
  }

  command(borne: Borne, action: 'start' | 'stop' | 'restart') {
    this.error = '';
    this.http.post(`/api/management/bornes/${encodeURIComponent(borne.borneId)}/${action}`, {}).subscribe({
      next: () => this.load(),
      error: () => this.error = `Unable to ${action} ${borne.borneId}.`
    });
  }

  ngOnDestroy() {
    this.destroyed.next();
    this.destroyed.complete();
  }
}

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient(withInterceptors([dashboardAuthInterceptor]))]
});
