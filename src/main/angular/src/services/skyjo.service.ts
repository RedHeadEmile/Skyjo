import {Injectable} from '@angular/core';
import {
    ApiService,
    SkyjoCurrentPlayerRoomDisplayNameRequestViewModel,
    SkyjoCurrentPlayerSettingsUpdateRequest,
    SkyjoGameActionViewModel,
    SkyjoGameActionViewModelType,
    SkyjoPlayerViewModel,
    SkyjoRoomMemberViewModel,
    SkyjoRoomViewModel,
    SkyjoRoomViewModelStatus
} from "./api.service";
import {lastValueFrom, Subject, Subscription} from "rxjs";
import {WebsocketService} from "./websocket.service";
import {Router} from "@angular/router";

export type LocalRoomServerMessageDiscriminator = 'gameCountdownStarted' | 'gameInterrupted' | 'internalError' | 'newCurrentDrawnCard' | 'newDiscardedCard' | 'newPlayerTurn' | 'playerDisplayNameChanged' | 'playerJoined' | 'playerLeave' | 'roomNameChanged' | 'selectingCardsPhase' | 'setPlayerCard' | 'setRoomOwner';
export type GlobalRoomServerMessageDiscriminator = 'destroyRoom' | 'newRoom' | 'roomNameChanged';

export type CardStatus = 'shown' | 'hidden' | 'deleted';

@Injectable({
  providedIn: 'root'
})
export class SkyjoService {

  private _timeoutSubject: Subject<string> = new Subject<string>(); // uuid of timed out player

  constructor(
    private readonly _apiService: ApiService,
    private readonly _websocketService: WebsocketService,
    private readonly _router: Router
  ) { }

  //#region CurrentPlayer
  private static readonly LOCALSTORAGE_PREFERRED_NAME_KEY = 'skyjo-preferred-name';

  private _currentPlayer?: SkyjoPlayerViewModel;

  public async refreshCurrentPlayer(): Promise<SkyjoPlayerViewModel> {
    return new Promise<SkyjoPlayerViewModel>(async accept => {
      this._currentPlayer = await lastValueFrom(this._apiService.showCurrentPlayer());
      const preferredName = localStorage.getItem(SkyjoService.LOCALSTORAGE_PREFERRED_NAME_KEY)?.trim();
      if (!!preferredName && preferredName.length > 3 && this._currentPlayer.displayName !== preferredName)
        await this.setCurrentPlayerDisplayName(preferredName);
      accept(this._currentPlayer);
    });
  }

  public get currentPlayer(): SkyjoPlayerViewModel | undefined {
    return this._currentPlayer;
  }

  public async setCurrentPlayerDisplayName(newDisplayName: string) {
    this._currentPlayer = await lastValueFrom(this._apiService.updateCurrentPlayerSettings(new SkyjoCurrentPlayerSettingsUpdateRequest({ displayName: newDisplayName })));
    localStorage.setItem(SkyjoService.LOCALSTORAGE_PREFERRED_NAME_KEY, newDisplayName);
  }
  //#endregion

  //#region CurrentRoom
  private _currentRoom?: SkyjoRoomViewModel;
  private _currentRoomDestroyer?: () => void;
  private _connectionLostSubscription?: Subscription;

  public async refreshCurrentRoom(roomId: string): Promise<SkyjoRoomViewModel> {
    if (!!this._currentRoom)
      throw new Error('A room is already set');

    this._currentRoom = await lastValueFrom(this._apiService.showRoom(roomId));
    this._currentRoomDestroyer = await this._websocketService.subscribe("/topic/rooms/" + this._currentRoom.id, msg => this._serverMessageHandler(msg));
    this._connectionLostSubscription = this._websocketService.connectionLostObservable.subscribe(() => this._handleConnectionLost());
    return this._currentRoom;
  }

  private async _handleConnectionLost() {
    if (!this._currentRoom)
      throw new Error('No current room, what are you subscribed to ?');

    alert("La connexion au serveur a été perdu");

    const room = await lastValueFrom(this._apiService.showRoom(this._currentRoom.id));
    // Room does not exist anymore
    if (!room) {
      await this.clearCurrentRoom();
      await this._router.navigate(['/rooms']);
      return;
    }

    this._currentRoom = room;
    this._currentRoomDestroyer = await this._websocketService.subscribe("/topic/rooms/" + this._currentRoom.id, msg => this._serverMessageHandler(msg));
    this._connectionLostSubscription = this._websocketService.connectionLostObservable.subscribe(() => this._handleConnectionLost());
  }

  public async clearCurrentRoom() {
    if (!!this._currentRoomDestroyer)
      this._currentRoomDestroyer();
    this._connectionLostSubscription?.unsubscribe();
    this._currentRoom = undefined;
    await lastValueFrom(this._apiService.deleteCurrentPlayerRoom());
  }

  public get currentRoom(): SkyjoRoomViewModel | undefined {
    return this._currentRoom;
  }

  public async setCurrentRoomName(newName: string) {
    if (!this._currentRoom)
      throw new Error('Not in a room.');

    await lastValueFrom(this._apiService.updateCurrentPlayerRoomDisplayName(
      new SkyjoCurrentPlayerRoomDisplayNameRequestViewModel({
        newDisplayName: newName
      })
    ));

    this._currentRoom.displayName = newName;
  }
  //#endregion

  //#region PlayerAction
  private _waitForNewDrawnCard: boolean = false;
  private _waitForNextTurn: boolean = false;
  private _waitForMyCardToFlip: boolean = false;

  get isActionAllowed(): boolean {
    return !this._waitForNewDrawnCard && !this._waitForNextTurn && !this._waitForMyCardToFlip;
  }

  public async doActionDrawACard() {
    if (!this.isActionAllowed) throw new Error('You can\'t do any action right now.');
    this._waitForNewDrawnCard = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.DRAW_A_CARD
    })));
  }

  public async doActionKeepDrawnCard(cardIndex: number) {
    if (!this.isActionAllowed) throw new Error('You can\'t do any action right now.');
    this._waitForNextTurn = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.EXCHANGE_WITH_DRAWN_CARD,
      cardIndex: cardIndex
    })))
  }

  public async doActionIgnoreDrawnCard() {
    if (!this.isActionAllowed) throw new Error('You can\'t do any action right now.');
    this._waitForNextTurn = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.IGNORE_DRAWN_CARD
    })));
  }

  public async doActionExchangeWithDiscardedCard(cardIndex: number) {
    if (!this.isActionAllowed) throw new Error('You can\'t do any action right now.');
    this._waitForNextTurn = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.EXCHANGE_WITH_DISCARDED_CARD,
      cardIndex: cardIndex
    })));
  }

  public async doActionFlipACard(cardIndex: number) {
    if (!this.isActionAllowed) throw new Error('You can\'t do any action right now.');

    if (this.currentRoom?.status === SkyjoRoomViewModelStatus.SELECTING_CARDS)
      this._waitForMyCardToFlip = true;
    else
      this._waitForNextTurn = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.FLIP_A_CARD,
      cardIndex: cardIndex
    })));
  }
  //#endregion

  private _serverMessageHandler(message: any) {
    if (typeof message !== 'object' || !message['discriminator'])
      throw new Error("Unable to handle the server message", {
        cause: message
      });

    if (!this._currentRoom)
      throw new Error("Unable to handle server message if not in a room");

    switch (message['discriminator'] as LocalRoomServerMessageDiscriminator) {
      case 'gameCountdownStarted': this._handleGameCountdownStartedMessage(message); break;
      case 'gameInterrupted': this._handleGameInterruptedMessage(message); break;
      case 'internalError': this._handleInternalErrorMessage(message); break;
      case 'newCurrentDrawnCard': this._handleNewCurrentDrawnCardMessage(message); break;
      case 'newDiscardedCard': this._handleNewDiscardedCardMessage(message); break;
      case 'newPlayerTurn': this._handleNewPlayerTurnMessage(message); break;
      case 'playerDisplayNameChanged': this._handlePlayerDisplayNameChangedMessage(message); break;
      case 'playerJoined': this._handlePlayerJoinedMessage(message); break;
      case 'playerLeave': this._handlePlayerLeaveMessage(message); break;
      case 'roomNameChanged': this._handleRoomNameChangedMessage(message); break;
      case 'selectingCardsPhase': this._handleSelectingCardsPhaseMessage(message); break;
      case 'setPlayerCard': this._handleSetPlayerCardMessage(message); break;
      case 'setRoomOwner': this._handleSetRoomOwnerMessage(message); break;
      default: throw new Error('Unknown action: ' + message['discriminator']);
    }
  }

  //#region MessageHandlers
  private _handleGameCountdownStartedMessage(message: any) {
    this._currentRoom!.gameBeginAt = message['gameBeginAt'];
  }

  private _handleGameInterruptedMessage(_: any) {
    this._currentRoom!.status = SkyjoRoomViewModelStatus.INTERRUPTED;
  }

  private _handleInternalErrorMessage(_: any) {
    alert('Le serveur à planté :(');
    this._currentRoom!.status = SkyjoRoomViewModelStatus.CRASHED;
  }

  private _handleNewCurrentDrawnCardMessage(message: any) {
    this._waitForNewDrawnCard = false;
    this._currentRoom!.currentDrawnCard = message['newCurrentDrawCardValue'];
  }

  private _handleNewDiscardedCardMessage(message: any) {
    this._currentRoom!.lastDiscardedCard = message['newDiscardedCardValue'];
  }

  private _handleNewPlayerTurnMessage(message: any) {
    this._waitForNextTurn = false;
    this._currentRoom!.status = SkyjoRoomViewModelStatus.TURN_IN_PROGRESS;

    if (message['previousPlayerWasTimedOut'])
      this._timeoutSubject.next(this._currentRoom!.currentTurnPlayerId!);

    this._currentRoom!.currentTurnPlayerId = message['newPlayerId'];
    this._currentRoom!.currentTurnEndAt = message['newPlayerTurnEndAt'];
  }

  private _handlePlayerDisplayNameChangedMessage(message: any) {
    const correspondingPlayer = this._currentRoom?.members.find(member => member.playerId === message['playerId']);
    if (!correspondingPlayer)
      throw new Error('Unknown player');

    correspondingPlayer.playerDisplayName = message['newDisplayName'];
  }

  private _handlePlayerJoinedMessage(message: any) {
    const newPlayer = new SkyjoRoomMemberViewModel({
      playerId: message['playerId'],
      playerDisplayName: message['displayName'],
      board: [],
      scores: []
    });

    this._currentRoom?.members.push(newPlayer);
  }

  private _handlePlayerLeaveMessage(message: any) {
    this._currentRoom!.members = this._currentRoom!.members.filter(member => member.playerId !== message['playerId']);
  }

  private _handleRoomNameChangedMessage(message: any) {
    this._currentRoom!.displayName = message['newDisplayName'];
  }

  private _handleSetRoomOwnerMessage(message: any) {
    this._currentRoom!.ownerId = message['newOwnerId'];
  }

  private _handleSelectingCardsPhaseMessage(_: any) {
    this._currentRoom!.status = SkyjoRoomViewModelStatus.SELECTING_CARDS;
  }

  private _handleSetPlayerCardMessage(message: any) {
    const playerId = message['playerId'];
    const cardIndex = message['cardIndex'];
    const cardValue = message['cardValue'];

    if (!this._currentRoom)
      throw new Error('Not in a room');

    const member = this._currentRoom.members.find(member => member.playerId === playerId);
    if (!member)
      throw new Error('Member not found');

    while (member.board.length < cardIndex)
      member.board.push(SkyjoService._HIDDEN_CARD);

    member.board[cardIndex] = cardValue;
    member.board = [...member.board];

    if (this._currentPlayer?.id === playerId)
      this._waitForMyCardToFlip = false;
  }
  //#endregion

  private static readonly _HIDDEN_CARD = -10;
  private static readonly _DELETED_CARD = -11;
  static getCardStatus(cardValue: any): CardStatus {
    if (cardValue === this._HIDDEN_CARD)
      return 'hidden';

    if (cardValue === this._DELETED_CARD)
      return 'deleted';

    if (typeof cardValue === 'number' && cardValue >= -2 && cardValue <= 12)
      return 'shown';

    return 'hidden';
  }
}
