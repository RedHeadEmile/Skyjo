import { Injectable } from '@angular/core';
import {ApiService, SkyjoRoomViewModel} from "./api.service";
import {lastValueFrom} from "rxjs";
import {WebsocketService} from "./websocket.service";

@Injectable({
  providedIn: 'root'
})
export class RoomStateService {

  private _currentRoom?: SkyjoRoomViewModel;

  constructor(
    private readonly _apiService: ApiService,
    private readonly _websocketService: WebsocketService
  ) { }

  public async refreshCurrentRoom(roomId: string): Promise<SkyjoRoomViewModel> {
    this._currentRoom = await lastValueFrom(this._apiService.showRoom(roomId));
    await this._websocketService.subscribeToRoom(this._currentRoom.id);
    return this._currentRoom;
  }

  public clearCurrentRoom() {
    this._currentRoom = undefined;
  }
}
