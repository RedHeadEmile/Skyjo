import { Injectable } from '@angular/core';
import {ApiService, SkyjoCurrentPlayerSettingsUpdateRequest, SkyjoPlayerViewModel} from "./api.service";
import {lastValueFrom} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private _currentUser?: SkyjoPlayerViewModel;
  private _currentUserPromise?: Promise<SkyjoPlayerViewModel>;

  constructor(
    private readonly _apiService: ApiService
  ) { }

  public async refreshUser(): Promise<SkyjoPlayerViewModel> {
    return this._currentUserPromise = new Promise<SkyjoPlayerViewModel>(async accept => {
      this._currentUser = await lastValueFrom(this._apiService.showCurrentPlayer());
      accept(this._currentUser);
    });
  }

  public get currentUser(): SkyjoPlayerViewModel | undefined {
    return this._currentUser;
  }

  public get currentUserPromise(): Promise<SkyjoPlayerViewModel> {
    if (!this._currentUserPromise)
      return this.refreshUser();
    return this._currentUserPromise;
  }

  public async setDisplayName(newDisplayName: string) {
    this._currentUser = await lastValueFrom(this._apiService.updateCurrentPlayerSettings(new SkyjoCurrentPlayerSettingsUpdateRequest({ displayName: newDisplayName })));
  }
}
