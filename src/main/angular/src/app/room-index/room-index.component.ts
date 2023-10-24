import {Component, OnInit} from '@angular/core';
import {
  ApiService,
  SkyjoCurrentPlayerRoomUpdateRequestViewModel,
  SkyjoRoomStoreRequestViewModel,
  SkyjoRoomViewModel
} from "../../services/api.service";
import {lastValueFrom} from "rxjs";
import {Router} from "@angular/router";
import {RoomStateService} from "../../services/room-state.service";

@Component({
  selector: 'app-room-index',
  templateUrl: './room-index.component.html',
  styleUrls: ['./room-index.component.scss']
})
export class RoomIndexComponent implements OnInit {
  rooms: SkyjoRoomViewModel[] = [];

  constructor(
    private readonly _apiService: ApiService,
    private readonly _router: Router,
    private readonly _roomStateService: RoomStateService
  ) {
  }

  async ngOnInit(): Promise<void> {
    this.rooms = await lastValueFrom(this._apiService.indexRoom());
  }

  async joinRoom(room: SkyjoRoomViewModel): Promise<void> {
    await lastValueFrom(this._apiService.updateCurrentPlayerRoom(new SkyjoCurrentPlayerRoomUpdateRequestViewModel({ roomSecretCode: room.secretCode })));
    await this._router.navigate(['/rooms', room.secretCode]);
  }

  async createRoom(): Promise<void> {
    const createdRoom = await lastValueFrom(this._apiService.storeRoom(new SkyjoRoomStoreRequestViewModel({ displayName: "My Room" })));
    await this._router.navigate(['/rooms', createdRoom.secretCode]);
  }
}
