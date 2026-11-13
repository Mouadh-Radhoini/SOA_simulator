import { describe, expect, it } from 'vitest';

describe('dashboard realtime contract', () => {
  it('uses the shared status, telemetry, and alert destinations', () => {
    const destinations = [
      '/topic/bornes/status',
      '/topic/bornes/telemetry',
      '/topic/alerts'
    ];

    expect(destinations).toContain('/topic/bornes/status');
    expect(destinations).toContain('/topic/bornes/telemetry');
    expect(destinations).toContain('/topic/alerts');
  });
});
