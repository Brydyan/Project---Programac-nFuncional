import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { UserProfileComponent } from './userprofile';
import { SessionService } from '../../Service/session.service';
import { UserService } from '../../Service/user.service';

class SessionServiceStub {
  getByToken() {
    return of({ userId: '1' });
  }
}

class UserServiceStub {
  getProfile() {
    return of({ id: '1', username: 'tester', email: 't@test.com' });
  }
  updateProfile() {
    return of({ id: '1', username: 'tester', email: 't@test.com' });
  }
}

describe('UserProfileComponent', () => {
  let component: UserProfileComponent;
  let fixture: ComponentFixture<UserProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserProfileComponent, RouterTestingModule],
      providers: [
        { provide: SessionService, useClass: SessionServiceStub },
        { provide: UserService, useClass: UserServiceStub }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load profile on init', () => {
    expect(component.user?.id).toBe('1');
    expect(component.loading).toBe(false);
  });
});
