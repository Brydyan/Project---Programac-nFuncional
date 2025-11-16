import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard, FormsModule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have searchText property', () => {
    expect(component.searchText).toBeDefined();
    expect(component.searchText).toBe('');
  });

  it('should have menuSections array with 4 items', () => {
    expect(component.menuSections).toBeDefined();
    expect(component.menuSections.length).toBe(4);
  });

  it('should call navigateToSection method', () => {
    spyOn(console, 'log');
    const section = { title: 'Test', icon: '📋', route: '/test' };
    component.navigateToSection(section);
    expect(console.log).toHaveBeenCalledWith('Navegando a:', '/test');
  });

  it('should call addFriend method', () => {
    spyOn(console, 'log');
    component.addFriend();
    expect(console.log).toHaveBeenCalledWith('Añadir amigo');
  });

  it('should call viewNotifications method', () => {
    spyOn(console, 'log');
    component.viewNotifications();
    expect(console.log).toHaveBeenCalledWith('Ver notificaciones');
  });

  it('should call getHelp method', () => {
    spyOn(console, 'log');
    component.getHelp();
    expect(console.log).toHaveBeenCalledWith('Ayuda o soporte');
  });

  it('should call onSearch method', () => {
    spyOn(console, 'log');
    component.searchText = 'test search';
    component.onSearch();
    expect(console.log).toHaveBeenCalledWith('Buscando:', 'test search');
  });

  it('should call logout method', () => {
    spyOn(console, 'log');
    component.logout();
    expect(console.log).toHaveBeenCalledWith('Cerrando sesión...');
  });
});
