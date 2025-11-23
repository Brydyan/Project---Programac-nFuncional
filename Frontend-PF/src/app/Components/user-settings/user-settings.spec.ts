import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { UserSettings } from './user-settings';

describe('UserSettings', () => {
  let component: UserSettings;
  let fixture: ComponentFixture<UserSettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserSettings, FormsModule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserSettings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default username', () => {
    expect(component.username).toBe('UsuarioPureChat');
  });

  it('should have status options', () => {
    expect(component.statusOptions).toContain('Online');
    expect(component.statusOptions.length).toBe(4);
  });

  it('should call saveChanges method', () => {
    spyOn(console, 'log');
    spyOn(window, 'confirm').and.returnValue(true);
    component.saveChanges();
    expect(console.log).toHaveBeenCalled();
  });

  it('should call resetDefaults method', () => {
    spyOn(window, 'alert');
    component.resetDefaults();
    expect(component.username).toBe('UsuarioPureChat');
    expect(component.selectedStatus).toBe('Online');
  });

  it('should have language options', () => {
    expect(component.languageOptions).toContain('español');
    expect(component.timezoneOptions).toContain('Europe/Madrid (GMT+1)');
  });
});