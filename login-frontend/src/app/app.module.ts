import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

@NgModule({
  imports: [
    BrowserModule,
    // NO DECLARAR componentes standalone aquí
  ],
  bootstrap: [] // vacío porque se usa bootstrap standalone
})
export class AppModule {}