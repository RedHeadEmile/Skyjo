import {Injectable} from '@angular/core';
import {
  ApiService,
  SkyjoCurrentPlayerRoomDisplayNameRequestViewModel,
  SkyjoGameActionViewModel,
  SkyjoGameActionViewModelType,
  SkyjoRoomMemberViewModel,
  SkyjoRoomViewModel,
  SkyjoRoomViewModelStatus
} from "./api.service";
import {lastValueFrom} from "rxjs";
import {WebsocketService} from "./websocket.service";
import {UserService} from "./user.service";

export type GameStatus = 'waiting-for-player' | 'selecting-cards' | 'turn-in-progress' | 'interrupted' | 'crashed';
export type ServerMessageDiscriminator = 'cardPicked' | 'flipPlayerCard' | 'gameCountdownStarted' | 'gameInterrupted' | 'internalError' | 'newDiscardedCard' | 'newPlayerTurn' | 'playerDisplayNameChanged' | 'playerJoined' | 'playerLeave' | 'roomNameChanged' | 'roomOwnerChanged' | 'selectingCardsPhase';

@Injectable({
  providedIn: 'root'
})
export class SkyjoGameService {

  static readonly HIDDEN_CARD = -10;
  static readonly DELETED_CARD = -11;

  private _gameStatus: GameStatus = 'waiting-for-player';
  private _currentRoom?: SkyjoRoomViewModel;
  private _personalMessage?: string;

  constructor(
    private readonly _apiService: ApiService,
    private readonly _userService: UserService,
    private readonly _websocketService: WebsocketService
  ) { }

  public async refreshCurrentRoom(roomId: string): Promise<SkyjoRoomViewModel> {
    this._currentRoom = await lastValueFrom(this._apiService.showRoom(roomId));
    this._gameStatus = 'waiting-for-player';
    await this._websocketService.subscribeToRoom(this._currentRoom.id, msg => this._serverMessageHandler(msg));
    return this._currentRoom;
  }

  public clearCurrentRoom() {
    this._currentRoom = undefined;
  }

  public get gameStatus(): GameStatus {
    return this._gameStatus;
  }

  public get currentRoom(): SkyjoRoomViewModel | undefined {
    return this._currentRoom;
  }

  public get personalMessage(): string | undefined {
    return this._personalMessage;
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

  //#region PlayerAction
  private _waitForServerResponse: boolean = false;

  public async pick() {
    if (this._waitForServerResponse) return;
    this._waitForServerResponse = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.PICK_A_CARD
    })));
  }

  public async keepPickedCard(cardIndex: number) {
    if (this._waitForServerResponse) return;
    this._waitForServerResponse = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.EXCHANGE_WITH_PICKED_CARD,
      cardIndex: cardIndex
    })))
  }

  public async ignorePickedCard() {
    if (this._waitForServerResponse) return;
    this._waitForServerResponse = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.IGNORE_PICKED_CARD
    })));
  }

  public async exchangeWithDiscardedCard(cardIndex: number) {
    if (this._waitForServerResponse) return;
    this._waitForServerResponse = true;

    await lastValueFrom(this._apiService.storeGameAction(new SkyjoGameActionViewModel({
      type: SkyjoGameActionViewModelType.EXCHANGE_WITH_DISCARDED_CARD,
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

    switch (message['discriminator'] as ServerMessageDiscriminator) {
      case 'cardPicked': this._handleCardPickedMessage(message); break;
      case 'flipPlayerCard': this._handleFlipPlayerCardMessage(message); break;
      case 'gameCountdownStarted': this._handleGameCountdownStartedMessage(message); break;
      case 'gameInterrupted': this._handleGameInterruptedMessage(message); break;
      case 'internalError': this._handleInternalErrorMessage(message); break;
      case 'newDiscardedCard': this._handleNewDiscardedCardMessage(message); break;
      case 'newPlayerTurn': this._handleNewPlayerTurnMessage(message); break;
      case 'playerDisplayNameChanged': this._handlePlayerDisplayNameChangedMessage(message); break;
      case 'playerJoined': this._handlePlayerJoinedMessage(message); break;
      case 'playerLeave': this._handlePlayerLeaveMessage(message); break;
      case 'roomNameChanged': this._handleRoomNameChangedMessage(message); break;
      case 'roomOwnerChanged': this._handleRoomOwnerChanged(message); break;
      case 'selectingCardsPhase': this._handleSelectingCardsPhaseMessage(message); break;
      default: throw new Error('Unkown action: ' + message['discriminator']);
    }
  }

  //#region MessageHandlers
  private _handleCardPickedMessage(message: any) {
    this._waitForServerResponse = false;
  }

  private _handleFlipPlayerCardMessage(message: any) {
    const playerId = message['playerId'];
    const cardIndex = message['cardIndex'];
    const cardValue = message['cardValue'];

    if (!this._currentRoom)
      throw new Error('Not in a room');

    const member = this._currentRoom.members.find(member => member.playerId === playerId);
    if (!member)
      throw new Error('Member not found');

    member.board[cardIndex] = cardValue;
    member.board = [...member.board];
  }

  private _handleGameCountdownStartedMessage(message: any) {
    this._currentRoom!.gameBeginAt = message['gameBeginAt'];
  }

  private _handleGameInterruptedMessage(message: any) {
    this._gameStatus = 'interrupted';
  }

  private _handleInternalErrorMessage(message: any) {
    alert('Le serveur à planté :(');
    this._gameStatus = 'crashed';
  }

  private _handleNewDiscardedCardMessage(message: any) {
    this._waitForServerResponse = false;
    this._currentRoom!.lastDiscardedCard = message['newDiscardedCardValue'];
  }

  private _handleNewPlayerTurnMessage(message: any) {
    this._waitForServerResponse = false;

    if (this._currentRoom?.currentTurnPlayerId === this._userService.currentUser?.id && message['previousPlayerWasTimedOut']) {
      this._personalMessage = 'Trop tard!';
      setTimeout(() => this._personalMessage = undefined, 2500);
    }

    this._currentRoom!.currentTurnPlayerId = message['newPlayerId'];
    this._currentRoom!.currentTurnPlayerEndAt = message['newPlayerTurnEndAt'];
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

  private _handleRoomOwnerChanged(message: any) {
    this._currentRoom!.ownerId = message['newOwnerId'];
  }

  private _handleSelectingCardsPhaseMessage(message: any) {
    this._currentRoom!.status = SkyjoRoomViewModelStatus.SELECTING_CARDS_PHASE;
    this._gameStatus = 'selecting-cards';
  }
  //#endregion
}
