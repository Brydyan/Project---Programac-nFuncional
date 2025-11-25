import { TestBed } from '@angular/core/testing';

import { ChannelsMsgService } from './channels-msg-service';

describe('ChannelsMsgService', () => {
  let service: ChannelsMsgService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ChannelsMsgService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
