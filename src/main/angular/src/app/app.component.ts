import {Component, OnInit} from '@angular/core';
import {UserService} from "../services/user.service";
import {WebsocketService} from "../services/websocket.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  username: string = '';

  constructor(
    private readonly _userService: UserService,
    private readonly _websocketService: WebsocketService
  ) {
  }

  async ngOnInit(): Promise<void> {
    this.username = (await this._userService.refreshUser()).displayName;
    await this._websocketService.init();
  }

  async onUsernameBlur(): Promise<void> {
    if (this.username === this._userService.currentUser?.displayName)
      return;

    this.username = this.username.trim();
    if (this.username.length < 3 || this.username.length > 32) {
      this.username = this._userService.currentUser?.displayName ?? '';
      return;
    }

    await this._userService.setDisplayName(this.username);
    this.username = this._userService.currentUser?.displayName ?? '';
  }
}
