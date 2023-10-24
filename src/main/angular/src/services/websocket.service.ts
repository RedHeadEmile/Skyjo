import { Injectable } from '@angular/core';
import {IMessage, RxStomp} from "@stomp/rx-stomp";
import {SettingsService} from "./settings.service";
import {Unsubscribable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class WebsocketService extends RxStomp {

  constructor(
    private readonly _settingsService: SettingsService
  ) {
    super();
  }

  private _stompSubscription?: Unsubscribable;

  async init(): Promise<void> {
    this._stompSubscription?.unsubscribe();
    await this.deactivate();

    this.configure({
      brokerURL: this._settingsService.WS_URL,

      reconnectDelay: 0,

      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,

      debug: (msg: string) => {
        console.log(msg);
      }
    });
  }

  public async subscribeToRoom(roomId: string): Promise<void> {
    await super.deactivate();
    super.activate();

    this._stompSubscription =
      super.watch({ destination: '/topic/rooms/' + roomId })
        .subscribe((message: IMessage) => this._handleMessage(message))
  }

  private _handleMessage(message: IMessage) {
    console.log(message.body);
  }
}
