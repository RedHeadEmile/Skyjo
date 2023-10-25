import {Component} from '@angular/core';
import {SkyjoGameService} from "../../services/skyjo-game.service";
import {UserService} from "../../services/user.service";
import {SkyjoRoomMemberViewModel} from "../../services/api.service";

@Component({
  selector: 'app-room-show',
  templateUrl: './room-show.component.html',
  styleUrls: ['./room-show.component.scss']
})
export class RoomShowComponent {
  constructor(
    private _gameService: SkyjoGameService,
    private _userService: UserService
  ) {
  }

  get iAmTheOwner(): boolean {
    return this._userService.currentUser?.id === this._gameService.currentRoom?.ownerId;
  }

  get ownerName(): string | undefined {
    return this._gameService.currentRoom?.members.find(member => member.playerId === this._gameService.currentRoom?.ownerId)?.playerDisplayName;
  }

  get otherMembers(): SkyjoRoomMemberViewModel[] {
    return this._gameService.currentRoom?.members.filter(member => member.playerId !== this._userService.currentUser?.id) ?? [];
  }

  get lastDiscardedCardValue(): string {
    const value = this._gameService.currentRoom?.lastDiscardedCard;
    if (!!value && value >= -2 && value <= 12)
      return value.toString();
    return '?';
  }

  //#region Me
  get myDisplayName(): string | undefined {
    return this._userService.currentUser?.displayName;
  }

  get iCanPlay(): boolean {
    if (this._gameService.gameStatus === 'selecting-cards')
      return this.myCards.filter(value => value >= -2 && value <= 12).length < 2;

    return this._gameService.currentRoom?.currentTurnPlayerId === this._userService.currentUser?.id;
  }

  get iCanPick(): boolean {
    return this._gameService.currentRoom?.currentTurnPlayerId === this._userService.currentUser?.id;
  }

  get myCards(): number[] {
    return this._gameService.currentRoom?.members.find(member => member.playerId === this._userService.currentUser?.id)?.board ?? [];
  }

  public async onCardClicked(cardIndex: number) {
    if (this._gameService.gameStatus === 'selecting-cards') {

    }
  }
  //#endregion

  //#region RoomName
  private _roomName?: string;
  public get roomName(): string | undefined {
    return this._roomName ?? this._gameService.currentRoom?.displayName;
  }

  public set roomName(value: string | undefined) {
    this._roomName = value;
  }

  public async onRoomNameBlur() {
    if (this._roomName === this._gameService.currentRoom?.displayName)
      return;

    this._roomName = this._roomName?.trim();
    if (!this._roomName || this._roomName.length < 3 || this._roomName.length > 32) {
      this._roomName = undefined;
      return;
    }

    await this._gameService.setCurrentRoomName(this._roomName);
    this._roomName = undefined;
  }
  //#endregion
}
