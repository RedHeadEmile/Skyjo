import { Injectable } from '@angular/core';
import {ApiService, SkyjoRoomViewModel} from "./api.service";
import {lastValueFrom} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class RoomStateService {

  private _currentRoom?: SkyjoRoomViewModel;

  constructor(
    private readonly _apiService: ApiService
  ) { }

  public async refreshCurrentRoom(roomId: string): Promise<SkyjoRoomViewModel> {
    this._currentRoom = await lastValueFrom(this._apiService.showRoom(roomId));
    return this._currentRoom;
  }

  public setCurrentRoom(room: SkyjoRoomViewModel): void {
    this._currentRoom = room;
  }

  public clearCurrentRoom() {
    this._currentRoom = undefined;
  }
}
