import { TestBed } from '@angular/core/testing';

import { ChannelEventsService } from './channel-events-service';

describe('ChannelEventsService', () => {
  let service: ChannelEventsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ChannelEventsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
