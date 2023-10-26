import {Injectable} from '@angular/core';
import {IMessage, RxStomp, RxStompState} from "@stomp/rx-stomp";
import {SettingsService} from "./settings.service";
import {Observable, Subject, Unsubscribable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class WebsocketService extends RxStomp {

  private readonly _connectionLostSubject: Subject<void> = new Subject<void>();

  constructor(
    private readonly _settingsService: SettingsService
  ) {
    super();
  }

  get connectionLostObservable(): Observable<void> {
    return this._connectionLostSubject.asObservable();
  }

  private _stompSubscriptions: { [key: string]: Unsubscribable } = {};

  private async _init(): Promise<void> {
    if (super.active)
      return;

    Object.keys(this._stompSubscriptions).forEach(key => this._stompSubscriptions[key].unsubscribe());
    this._stompSubscriptions = {};

    await this.deactivate();

    this.configure({
      brokerURL: this._settingsService.WS_URL,

      reconnectDelay: 0,

      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000
    });

    super.activate();
  }

  public async subscribe(destination: string, handler: (message: any) => void): Promise<void> {
    await this._init();

    if (!!this._stompSubscriptions[destination])
      throw new Error('Destination already subscribed');

    this._stompSubscriptions[destination] =
      super.watch({ destination: destination })
        .subscribe((message: IMessage) => {
          try {
            const obj = JSON.parse(message.body);
            handler(obj);
          }
          catch (_) {
            handler(message.body);
          }
        })
  }
}
