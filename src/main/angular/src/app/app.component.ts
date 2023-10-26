import {Component, OnInit} from '@angular/core';
import {SkyjoService} from "../services/skyjo.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  username: string = '';

  constructor(
    private readonly _skyjoService: SkyjoService
  ) {
  }

  async ngOnInit(): Promise<void> {
    this.username = (await this._skyjoService.refreshCurrentPlayer()).displayName;
  }

  async onUsernameBlur(): Promise<void> {
    if (this.username === this._skyjoService.currentPlayer?.displayName)
      return;

    this.username = this.username.trim();
    if (this.username.length < 3 || this.username.length > 32) {
      this.username = this._skyjoService.currentPlayer?.displayName ?? '';
      return;
    }

    await this._skyjoService.setCurrentPlayerDisplayName(this.username);
    this.username = this._skyjoService.currentPlayer?.displayName ?? '';
  }
}
