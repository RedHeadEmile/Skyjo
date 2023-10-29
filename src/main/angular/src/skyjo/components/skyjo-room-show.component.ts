import {Component, OnDestroy, OnInit} from '@angular/core';
import {SkyjoService} from "../services/skyjo.service";
import {SkyjoRoomMemberViewModel, SkyjoRoomViewModelStatus} from "../../services/api.service";
import {Router} from "@angular/router";

@Component({
  selector: 'skyjo-room-show',
  templateUrl: './skyjo-room-show.component.html',
  styleUrls: ['./skyjo-room-show.component.scss', './skyjo.scss']
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

  //#region Room owner identity
  get iAmTheOwner(): boolean {
    return this._gameService.currentPlayer?.id === this._gameService.currentRoom?.ownerId;
  }

  get ownerName(): string | undefined {
    return this._gameService.currentRoom?.members.find(member => member.playerId === this._gameService.currentRoom?.ownerId)?.playerDisplayName;
  }
  //#endregion

  get roomMembersWithoutMe(): SkyjoRoomMemberViewModel[] {
    return this._gameService.currentRoom?.members.filter(member => member.playerId !== this._gameService.currentPlayer?.id) ?? [];
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
  //#endregion

  //#region Waiting for players phase
  get countdownBeforeStart(): number | undefined {
    if (!this._gameService.currentRoom?.gameBeginAt)
      return undefined;

    return Math.round(Math.max(0, this._gameService.currentRoom.gameBeginAt - this._now) / 1000);
  }
  //#endregion

  //#region CurrentTurn
  get isItMyTurn(): boolean {
    return this._gameService.currentRoom?.currentTurnPlayerId === this._gameService.currentPlayer?.id;
  }

  get currentTurnPlayerName(): string | undefined {
    return this._gameService.currentRoom?.members.find(member => member.playerId === this._gameService.currentRoom?.currentTurnPlayerId)?.playerDisplayName;
  }

  get currentTurnRemainingTime(): number | undefined {
    if (!this._gameService.currentRoom?.currentTurnEndAt)
      return undefined;

    return Math.round(Math.max(0, this._gameService.currentRoom.currentTurnEndAt - this._now) / 1000);
  }
  //#endregion

  //#region Deck
  get topDeckCard(): string {
    if (SkyjoService.getCardStatus(this._gameService.currentRoom?.currentDrawnCard) === 'shown')
      return this._gameService.currentRoom!.currentDrawnCard!.toString();
    return '?';
  }

  get iCanPickTopDeckCard(): boolean {
    return this.isItMyTurn && SkyjoService.getCardStatus(this._gameService.currentRoom?.currentDrawnCard) === 'hidden' && this._gameService.isActionAllowed && !this._playerWannaDiscardACard;
  }

  public async onTopDeckCardClicked() {
    if (!this.iCanPickTopDeckCard)
      return;

    if (typeof this._gameService.currentRoom?.currentDrawnCard !== 'number')
      await this._gameService.doActionDrawACard();
  }

  get lastDiscardedCard(): string {
    if (SkyjoService.getCardStatus(this._gameService.currentRoom?.lastDiscardedCard) === 'shown')
      return this._gameService.currentRoom!.lastDiscardedCard!.toString();
    return '?';
  }

  get iCanPickDiscardedCard(): boolean {
    return this.isItMyTurn && this._gameService.isActionAllowed;
  }

  private _playerWannaDiscardACard: boolean = false;
  public async onDiscardedCardClicked() {
    if (!this.iCanPickDiscardedCard)
      return;

    if (SkyjoService.getCardStatus(this._gameService.currentRoom?.currentDrawnCard) === 'hidden')
      this._playerWannaDiscardACard = true;
    else
      await this._gameService.doActionIgnoreDrawnCard();
  }
  //#endregion

  //#region MyCards
  get pickableCardsForMe(): boolean[] {
    const allFalseValues = [false, false, false, false, false, false, false, false, false, false, false, false];

    if (!this._gameService.isActionAllowed)
      return allFalseValues;

    if (this.isStatusSelectingCards && this.myCards.filter(value => SkyjoService.getCardStatus(value) === 'shown').length >= 2)
        return allFalseValues;

    if (this.isStatusTurnInProgress && !this.isItMyTurn)
      return allFalseValues;

    if (!this.isStatusSelectingCards && !this.isStatusTurnInProgress)
      return allFalseValues;

    const pickableCards: boolean[] = [];
    for (let cardIndex of [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]) {
      const cardStatus = SkyjoService.getCardStatus(this.myCards.find((_, i) => i === cardIndex));

      if (cardStatus === 'deleted')
        pickableCards.push(false);

      else if (this._gameService.currentRoom?.status === SkyjoRoomViewModelStatus.SELECTING_CARDS)
        pickableCards.push(cardStatus === 'hidden');

      else {
        pickableCards.push(
            SkyjoService.getCardStatus(this._gameService.currentRoom?.currentDrawnCard) === 'shown'
            || this._playerWannaDiscardACard
            || cardStatus === 'hidden'
        );
      }
    }
    return pickableCards;
  }

  get myCards(): number[] {
    return this._gameService.currentRoom?.members.find(member => member.playerId === this._gameService.currentPlayer?.id)?.board ?? [];
  }

  public async onMyCardClicked(cardIndex: number) {
    if (!this.pickableCardsForMe[cardIndex])
      return;

    if (this.isStatusSelectingCards)
      await this._gameService.doActionFlipACard(cardIndex);

    else if (this.isStatusTurnInProgress) {
      if (this._playerWannaDiscardACard) {
        await this._gameService.doActionExchangeWithDiscardedCard(cardIndex);
        this._playerWannaDiscardACard = false;
      }
      else if (SkyjoService.getCardStatus(this._gameService.currentRoom?.currentDrawnCard) === 'shown')
        await this._gameService.doActionKeepDrawnCard(cardIndex);
      else
        await this._gameService.doActionFlipACard(cardIndex);
    }
  }
  //#endregion

  public async leave() {
    await this._gameService.clearCurrentRoom();
    await this._router.navigate(['/rooms']);
  }

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
