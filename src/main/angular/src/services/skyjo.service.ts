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
import {lastValueFrom} from "rxjs";
import {WebsocketService} from "./websocket.service";

export type ServerMessageDiscriminator = 'cardPicked' | 'gameCountdownStarted' | 'gameInterrupted' | 'internalError' | 'newCurrentDrawnCard' | 'newDiscardedCard' | 'newPlayerTurn' | 'playerDisplayNameChanged' | 'playerJoined' | 'playerLeave' | 'roomNameChanged' | 'roomOwnerChanged' | 'selectingCardsPhase' | 'setPlayerCard';

@Injectable({
  providedIn: 'root'
})
export class SkyjoService {

  static readonly HIDDEN_CARD = -10;
  static readonly DELETED_CARD = -11;

  private _personalMessage?: string;

  constructor(
    private readonly _apiService: ApiService,
    private readonly _websocketService: WebsocketService
  ) { }

  //#region CurrentPlayer
  private static readonly LOCALSTORAGE_PREFERRED_NAME_KEY = 'skyjo-preferred-name';

  private _currentPlayer?: SkyjoPlayerViewModel;
  private _currentPlayerPromise?: Promise<SkyjoPlayerViewModel>;

  public async refreshCurrentPlayer(): Promise<SkyjoPlayerViewModel> {
    return this._currentPlayerPromise = new Promise<SkyjoPlayerViewModel>(async accept => {
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

  public get currentUserPromise(): Promise<SkyjoPlayerViewModel> {
    return this._currentPlayerPromise ?? this.refreshCurrentPlayer();
  }

  public async setCurrentPlayerDisplayName(newDisplayName: string) {
    this._currentPlayer = await lastValueFrom(this._apiService.updateCurrentPlayerSettings(new SkyjoCurrentPlayerSettingsUpdateRequest({ displayName: newDisplayName })));
    localStorage.setItem(SkyjoService.LOCALSTORAGE_PREFERRED_NAME_KEY, newDisplayName);
  }
  //#endregion

  public get personalMessage(): string | undefined {
    return this._personalMessage;
  }

  //#region CurrentRoom
  private _currentRoom?: SkyjoRoomViewModel;
  public async refreshCurrentRoom(roomId: string): Promise<SkyjoRoomViewModel> {
    this._currentRoom = await lastValueFrom(this._apiService.showRoom(roomId));
    await this._websocketService.subscribe("/topic/rooms/" + this._currentRoom.id, msg => this._serverMessageHandler(msg));
    return this._currentRoom;
  }

  public clearCurrentRoom() {
    this._currentRoom = undefined;
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

  public async flipACard(cardIndex: number) {
    if (this._waitForServerResponse) return;
    this._waitForServerResponse = true;

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

    switch (message['discriminator'] as ServerMessageDiscriminator) {
      case 'cardPicked': this._handleCardPickedMessage(message); break;
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
      case 'roomOwnerChanged': this._handleRoomOwnerChanged(message); break;
      case 'selectingCardsPhase': this._handleSelectingCardsPhaseMessage(message); break;
      case 'setPlayerCard': this._handleSetPlayerCardMessage(message); break;
      default: throw new Error('Unkown action: ' + message['discriminator']);
    }
  }

  //#region MessageHandlers
  private _handleCardPickedMessage(message: any) {
    this._waitForServerResponse = false;
  }

  private _handleGameCountdownStartedMessage(message: any) {
    this._currentRoom!.gameBeginAt = message['gameBeginAt'];
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handleGameInterruptedMessage(message: any) {
    this._currentRoom!.status = SkyjoRoomViewModelStatus.INTERRUPTED;
  }

  private _handleInternalErrorMessage(message: any) {
    alert('Le serveur à planté :(');
    this._currentRoom!.status = SkyjoRoomViewModelStatus.CRASHED;
  }

  private _handleNewCurrentDrawnCardMessage(message: any) {
    this._currentRoom!.currentDrawnCard = message['newCurrentDrawCardValue'];
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handleNewDiscardedCardMessage(message: any) {
    this._waitForServerResponse = false;
    this._currentRoom!.lastDiscardedCard = message['newDiscardedCardValue'];
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handleNewPlayerTurnMessage(message: any) {
    this._waitForServerResponse = false;
    this._currentRoom!.status = SkyjoRoomViewModelStatus.TURN_IN_PROGRESS;

    if (this._currentRoom?.currentTurnPlayerId === this.currentPlayer?.id && message['previousPlayerWasTimedOut']) {
      this._personalMessage = 'Trop tard!';
      setTimeout(() => this._personalMessage = undefined, 2500);
    }

    this._currentRoom!.currentTurnPlayerId = message['newPlayerId'];
    this._currentRoom!.currentTurnEndAt = message['newPlayerTurnEndAt'];
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handlePlayerDisplayNameChangedMessage(message: any) {
    const correspondingPlayer = this._currentRoom?.members.find(member => member.playerId === message['playerId']);
    if (!correspondingPlayer)
      throw new Error('Unknown player');

    correspondingPlayer.playerDisplayName = message['newDisplayName'];
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handlePlayerJoinedMessage(message: any) {
    const newPlayer = new SkyjoRoomMemberViewModel({
      playerId: message['playerId'],
      playerDisplayName: message['displayName'],
      board: [],
      scores: []
    });

    this._currentRoom?.members.push(newPlayer);
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handlePlayerLeaveMessage(message: any) {
    this._currentRoom!.members = this._currentRoom!.members.filter(member => member.playerId !== message['playerId']);
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handleRoomNameChangedMessage(message: any) {
    this._currentRoom!.displayName = message['newDisplayName'];
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handleRoomOwnerChanged(message: any) {
    this._currentRoom!.ownerId = message['newOwnerId'];
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
  }

  private _handleSelectingCardsPhaseMessage(message: any) {
    this._currentRoom!.status = SkyjoRoomViewModelStatus.SELECTING_CARDS;
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);
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
      member.board.push(-10);

    member.board[cardIndex] = cardValue;
    member.board = [...member.board];
    this._currentRoom = new SkyjoRoomViewModel(this._currentRoom);

    if (this._currentRoom.status === SkyjoRoomViewModelStatus.SELECTING_CARDS && this._currentPlayer?.id === playerId)
      this._waitForServerResponse = false;
  }
  //#endregion
}
