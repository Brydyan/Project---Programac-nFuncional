import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChannelsThread } from './channels-thread';

describe('ChannelsThread', () => {
  let component: ChannelsThread;
  let fixture: ComponentFixture<ChannelsThread>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChannelsThread]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChannelsThread);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});