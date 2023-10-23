import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {lastValueFrom} from "rxjs";

type Settings = {
  apiBaseUrl: string;
  wsUrl: string;
};

@Injectable({
  providedIn: 'root'
})
export class SettingsService {

  public API_BASE_URL: string = '';
  public WS_URL: string = '';

  constructor(private _httpClient: HttpClient) { }

  public async loadSettings(): Promise<void> {
    const settings = await lastValueFrom(this._httpClient.get('/assets/settings.json?_' + (new Date().getTime()))) as Settings;
    this.API_BASE_URL = settings.apiBaseUrl;
    this.WS_URL = settings.wsUrl;
  }
}
