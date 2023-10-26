import {Component, OnDestroy, OnInit} from '@angular/core';
import {SkyjoService} from "../../services/skyjo.service";
import {SkyjoRoomMemberViewModel, SkyjoRoomViewModelStatus} from "../../services/api.service";
import {Unsubscribable} from "rxjs";
import {Router} from "@angular/router";

@Component({
  selector: 'skyjo-room-show',
  templateUrl: './skyjo-room-show.component.html',
  styleUrls: ['./skyjo-room-show.component.scss']
})
export class SkyjoRoomShowComponent implements OnInit, OnDestroy {
  constructor(
    private _gameService: SkyjoService,
    private _router: Router
  ) {
  }

  private _now: number = 0;
  private _timeInterval?: any;
  ngOnInit() {
    this._timeInterval = setInterval(() => this._now = Date.now(), 100);
  }

  ngOnDestroy() {
    clearInterval(this._timeInterval);
  }

  //#region GameStatus
  get isStatusWaitingForPlayers(): boolean {
    return this._gameService.currentRoom?.status === SkyjoRoomViewModelStatus.WAITING_FOR_PLAYERS;
  }

  get isStatusSelectingCards(): boolean {
    return this._gameService.currentRoom?.status === SkyjoRoomViewModelStatus.SELECTING_CARDS;
  }

  get isStatusTurnInProgress(): boolean {
    return this._gameService.currentRoom?.status === SkyjoRoomViewModelStatus.TURN_IN_PROGRESS;
  }

  get currentTurnPlayerName(): string | undefined {
    return this._gameService.currentRoom?.members.find(member => member.playerId === this._gameService.currentRoom?.currentTurnPlayerId)?.playerDisplayName;
  }
  //#endregion

  //#region Countdown
  get countdownBeforeStart(): number | undefined {
    if (!this._gameService.currentRoom?.gameBeginAt)
      return undefined;

    return Math.max(0, this._gameService.currentRoom.gameBeginAt - this._now) / 1000;
  }
  //#endregion

  get iAmTheOwner(): boolean {
    return this._gameService.currentPlayer?.id === this._gameService.currentRoom?.ownerId;
  }

  get ownerName(): string | undefined {
    return this._gameService.currentRoom?.members.find(member => member.playerId === this._gameService.currentRoom?.ownerId)?.playerDisplayName;
  }

  get otherMembers(): SkyjoRoomMemberViewModel[] {
    return this._gameService.currentRoom?.members.filter(member => member.playerId !== this._gameService.currentPlayer?.id) ?? [];
  }

  get topDeckCard(): string {
    return this._gameService.currentRoom?.currentDrawnCard?.toString() ?? '?';
  }

  get lastDiscardedCardValue(): string {
    const value = this._gameService.currentRoom?.lastDiscardedCard;
    if (typeof value === "number" && value >= -2 && value <= 12)
      return value.toString();
    return '?';
  }

  //#region Me
  public async leave() {
    await this._gameService.clearCurrentRoom();
    await this._router.navigate(['/rooms']);
  }

  get iCanPickTopDeckCard(): boolean {
    return this._gameService.currentRoom?.currentTurnPlayerId === this._gameService.currentPlayer?.id && !this._gameService.currentRoom?.currentDrawnCard;
  }

  get iCanPickDiscardedCard(): boolean {
    return this._gameService.currentRoom?.currentTurnPlayerId === this._gameService.currentPlayer?.id;
  }

  get iCanPickMyCards(): boolean {
    if (this._gameService.currentRoom?.status === SkyjoRoomViewModelStatus.SELECTING_CARDS)
      return this.myCards.filter(value => value >= -2 && value <= 12).length < 2;

    return this._gameService.currentRoom?.currentTurnPlayerId === this._gameService.currentPlayer?.id;
  }

  get myCards(): number[] {
    return this._gameService.currentRoom?.members.find(member => member.playerId === this._gameService.currentPlayer?.id)?.board ?? [];
  }

  public async onCardClicked(cardIndex: number) {
    if (this._gameService.currentRoom?.status === SkyjoRoomViewModelStatus.SELECTING_CARDS && this.myCards.filter(value => value >= -2 && value <= 12).length < 2)
      await this._gameService.flipACard(cardIndex);

    else if (this._gameService.currentRoom?.status === SkyjoRoomViewModelStatus.TURN_IN_PROGRESS && this._gameService.currentRoom?.currentTurnPlayerId === this._gameService.currentPlayer?.id) {
      if (!!this._gameService.currentRoom?.currentDrawnCard)
        await this._gameService.keepPickedCard(cardIndex);
      else {
        const myCards = this.myCards;
        if (myCards.length > cardIndex && (myCards[cardIndex] >= -2 && myCards[cardIndex] <= 12))
          return;
        await this._gameService.flipACard(cardIndex);
      }
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
