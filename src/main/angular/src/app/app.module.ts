import {APP_INITIALIZER, NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { RoomIndexComponent } from './skyjo-room-index/room-index.component';
import { SkyjoRoomShowComponent } from './skyjo-room-show/skyjo-room-show.component';
import {SettingsService} from "../services/settings.service";
import {API_BASE_URL} from "../services/api.service";
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import { SkyjoBoardComponent } from './skyjo-board/skyjo-board.component';

@NgModule({
  declarations: [
    AppComponent,
    RoomIndexComponent,
    SkyjoRoomShowComponent,
    SkyjoBoardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [
    SettingsService,
    {
      provide: APP_INITIALIZER,
      useFactory: (settingsService: SettingsService) => () => settingsService.loadSettings(),
      deps: [SettingsService],
      multi: true
    },
    {
      provide: API_BASE_URL,
      useFactory: (settingsService: SettingsService) => settingsService.API_BASE_URL,
      deps: [SettingsService]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
