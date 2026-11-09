import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

type Borne = { borneId: string; containerName: string; containerStatus: string; applicationHealthStatus: string; consecutiveFailures: number; lastHealthCheck: string };

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  template: `<main><h1>SOA Borne Dashboard</h1><button (click)="load()">Refresh</button>
  <section class="cards"><b>Total {{bornes.length}}</b><b>Healthy {{healthy}}</b><b>Stopped {{stopped}}</b></section>
  <table><tr><th>Borne</th><th>Container</th><th>Technical health</th><th>Status</th><th>Actions</th></tr>
  <tr *ngFor="let b of bornes"><td>{{b.borneId}}</td><td>{{b.containerName}}</td><td>{{b.applicationHealthStatus}}</td><td>{{b.containerStatus}}</td>
  <td><button (click)="command(b,'start')">Start</button><button (click)="command(b,'stop')">Stop</button><button (click)="command(b,'restart')">Restart</button></td></tr></table></main>`
})
export class AppComponent {
  private http = inject(HttpClient); bornes: Borne[] = [];
  get healthy() { return this.bornes.filter(b => b.applicationHealthStatus === 'HEALTHY').length; }
  get stopped() { return this.bornes.filter(b => b.applicationHealthStatus === 'STOPPED').length; }
  constructor() { this.load(); }
  load() { this.http.get<Borne[]>('/api/management/bornes').subscribe({next: value => this.bornes = value}); }
  command(b: Borne, action: string) { this.http.post(`/api/management/bornes/${b.borneId}/${action}`, {}).subscribe(() => this.load()); }
}
bootstrapApplication(AppComponent, { providers: [provideHttpClient()] });
