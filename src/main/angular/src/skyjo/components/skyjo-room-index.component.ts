import {Component, OnDestroy, OnInit} from '@angular/core';
import {
  ApiService,
  SkyjoCurrentPlayerRoomUpdateRequestViewModel,
  SkyjoRoomStoreRequestViewModel,
  SkyjoRoomViewModel
} from "../../services/api.service";
import {lastValueFrom} from "rxjs";
import {Router} from "@angular/router";
import {WebsocketService} from "../../services/websocket.service";
import {GlobalRoomServerMessageDiscriminator, SkyjoService} from "../services/skyjo.service";

@Component({
  selector: 'skyjo-room-index',
  templateUrl: './skyjo-room-index.component.html',
  styleUrls: ['./skyjo-room-index.component.scss']
})
export class SkyjoRoomIndexComponent implements OnInit, OnDestroy {

  rooms: SkyjoRoomViewModel[] = [];
  private _roomsObserverDestroyer?: () => void;

  constructor(
    private readonly _apiService: ApiService,
    private readonly _router: Router,
    private readonly _skyjoService: SkyjoService,
    private readonly _websocketService: WebsocketService
  ) {
  }

  async ngOnInit(): Promise<void> {
    this.rooms = await lastValueFrom(this._apiService.indexRoom());
    this._roomsObserverDestroyer = await this._websocketService.subscribe('/topic/rooms', (message) => {
      if (typeof message !== 'object' || !message['discriminator'])
        throw new Error("Unable to handle the server message", {
          cause: message
        });

      switch (message['discriminator'] as GlobalRoomServerMessageDiscriminator) {
        case 'destroyRoom':
          this.rooms = this.rooms.filter(room => room.id !== message['roomId']);
          break;

        case 'newRoom':
          this.rooms.push(message['newRoom']);
          break;

        case 'roomNameChanged':
          const room = this.rooms.find(room => room.id === message['roomId']);
          if (!room)
            throw new Error('Unknown room');

          room.displayName = message['newDisplayName'];
          break;

        default: throw new Error('Unknown action: ' + message['discriminator']);
      }
    });
  }

  ngOnDestroy() {
    if (this._roomsObserverDestroyer !== undefined)
      this._roomsObserverDestroyer();
  }

  async joinRoom(room: SkyjoRoomViewModel): Promise<void> {
    await lastValueFrom(this._apiService.updateCurrentPlayerRoom(new SkyjoCurrentPlayerRoomUpdateRequestViewModel({ roomSecretCode: room.secretCode })));
    await this._router.navigate(['/rooms', room.secretCode]);
  }

  async createRoom(): Promise<void> {
    const createdRoom = await lastValueFrom(this._apiService.storeRoom(new SkyjoRoomStoreRequestViewModel({ displayName: this._skyjoService.currentPlayer?.displayName + "'s room" })));
    await this._router.navigate(['/rooms', createdRoom.secretCode]);
  }
}
