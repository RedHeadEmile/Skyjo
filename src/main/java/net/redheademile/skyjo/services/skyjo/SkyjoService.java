package net.redheademile.skyjo.services.skyjo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.redheademile.skyjo.services.async.IAsyncService;
import net.redheademile.skyjo.services.skyjo.business.models.*;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoPlayerDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomMemberDataModel;
import net.redheademile.skyjo.services.skyjo.repositories.ISkyjoRepository;
import net.redheademile.skyjo.services.skyjo.websocket.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static net.redheademile.skyjo.utils.Mapping.map;

@Service
@RequestScope
public class SkyjoService implements ISkyjoService {

    private final Logger logger = LoggerFactory.getLogger(SkyjoService.class);

    private final IAsyncService asyncService;
    private final ISkyjoRepository skyjoRepository;

    private boolean isInitialized = false;
    private SkyjoPlayerBusinessModel currentPlayer;


    private static final String AUTH_COOKIE_NAME = "SkyjoToken";
    private static final Integer DELETED_CARD = -11;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public SkyjoService(
            IAsyncService asyncService,
            ISkyjoRepository skyjoRepository,

            HttpServletRequest request,
            HttpServletResponse response,

            SimpMessagingTemplate simpMessagingTemplate
    ) {
        this.asyncService = asyncService;
        this.skyjoRepository = skyjoRepository;
        this.request = request;
        this.response = response;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    //#region Rooms
    @Override
    public SkyjoRoomBusinessModel addRoom(String displayName) {
        UUID uuid;
        String secretCode;

        //#region Find valid UUID & SecretCode
        SkyjoRoomDataModel existingRoom;
        do {
            uuid = UUID.randomUUID();
            existingRoom = skyjoRepository.readRoom(uuid);
        }
        while (existingRoom != null);

        do {
            secretCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            existingRoom = skyjoRepository.readRoom(secretCode);
        }
        while (existingRoom != null);
        //#endregion

        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        //#region Create new room
        SkyjoRoomBusinessModel newRoom = new SkyjoRoomBusinessModel();
        newRoom.setId(uuid);
        newRoom.setSecretCode(secretCode);
        newRoom.setDisplayName(displayName);
        newRoom.setOwnerId(me.getId());
        newRoom.setStatus(ESkyjoRoomStatusBusinessModel.WAITING_FOR_PLAYERS);

        skyjoRepository.createRoom(newRoom.toDataModel());
        //#endregion

        //#region Add currentPlayer to the room
        removeCurrentPlayerFromRoom();

        SkyjoRoomMemberBusinessModel member = new SkyjoRoomMemberBusinessModel();
        member.setRoom(newRoom);
        member.setPlayer(me);

        skyjoRepository.createRoomMember(member.toDataModel());
        //#endregion

        // Start the game engine for this room
        asyncService.runAsync(() -> runGameEngine(newRoom.getId()));
        
        return newRoom;
    }

    private void fillRoom(SkyjoRoomBusinessModel room) {
        room.setMembers(map(skyjoRepository.readRoomMembers(room.getId()), memberDataModel -> {
            SkyjoRoomMemberBusinessModel member = SkyjoRoomMemberBusinessModel.fromDataModel(memberDataModel);
            member.setRoom(room);
            member.setPlayer(SkyjoPlayerBusinessModel.fromDataModel(skyjoRepository.readPlayer(memberDataModel.getPlayerId())));
            return member;
        }));
    }

    @Override
    public SkyjoRoomBusinessModel getRoom(UUID roomId) {
        SkyjoRoomDataModel dataModel = skyjoRepository.readRoom(roomId);
        if (dataModel == null)
            return null;

        SkyjoRoomBusinessModel room = SkyjoRoomBusinessModel.fromDataModel(dataModel);
        fillRoom(room);

        return room;
    }

    @Override
    public SkyjoRoomBusinessModel getRoom(String roomSecretCode) {
        SkyjoRoomDataModel dataModel = skyjoRepository.readRoom(roomSecretCode);
        if (dataModel == null)
            return null;

        SkyjoRoomBusinessModel room = SkyjoRoomBusinessModel.fromDataModel(dataModel);
        fillRoom(room);

        return room;
    }

    @Override
    public List<SkyjoRoomBusinessModel> getRooms() {
        return map(skyjoRepository.readRooms(), roomDataModel -> {
            SkyjoRoomBusinessModel room = SkyjoRoomBusinessModel.fromDataModel(roomDataModel);
            fillRoom(room);
            return room;
        });
    }
    //#endregion

    //#region CurrentPlayer
    private SkyjoPlayerBusinessModel newCurrentPlayer() {
        UUID uuid;
        SkyjoPlayerDataModel existingPlayer;

        do {
            uuid = UUID.randomUUID();
            existingPlayer = skyjoRepository.readPlayer(uuid);
        }
        while (existingPlayer != null);

        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, uuid.toString());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        response.addCookie(cookie);

        SkyjoPlayerDataModel newPlayer = new SkyjoPlayerDataModel();
        newPlayer.setId(uuid);
        newPlayer.setDisplayName("Steve");

        skyjoRepository.createPlayer(newPlayer);

        SkyjoPlayerBusinessModel me = SkyjoPlayerBusinessModel.fromDataModel(newPlayer);
        this.isInitialized = true;
        this.currentPlayer = me;

        return me;
    }

    @Override
    public SkyjoPlayerBusinessModel getCurrentPlayer() {
        if (isInitialized)
            return currentPlayer;

        if (request.getCookies() == null)
            return newCurrentPlayer();

        Cookie tokenCookie = null;
        for (Cookie cookie : request.getCookies())
            if (cookie.getName().equals(AUTH_COOKIE_NAME)) {
                tokenCookie = cookie;
                break;
            }

        if (tokenCookie == null || tokenCookie.getValue() == null)
            return newCurrentPlayer();

        String authToken = tokenCookie.getValue();
        try {
            UUID uuid = UUID.fromString(authToken);
            SkyjoPlayerDataModel dataModel = this.skyjoRepository.readPlayer(uuid);
            if (dataModel == null)
                return newCurrentPlayer();

            SkyjoPlayerBusinessModel businessModel = SkyjoPlayerBusinessModel.fromDataModel(dataModel);
            this.isInitialized = true;
            this.currentPlayer = businessModel;

            return this.currentPlayer;
        }
        catch (IllegalArgumentException ignored) { }

        return newCurrentPlayer();
    }

    @Override
    public SkyjoPlayerBusinessModel setCurrentPlayerName(String displayName) {
        SkyjoPlayerBusinessModel me = getCurrentPlayer();
        me.setDisplayName(displayName);

        skyjoRepository.updatePlayer(me.toDataModel());

        SkyjoRoomMemberDataModel member = skyjoRepository.readRoomMember(me.getId());
        if (member != null)
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + member.getRoomId(), new PlayerDisplayNameChangedWebsocketModel(me.getId(), displayName));

        return me;
    }

    @Override
    public SkyjoRoomBusinessModel addCurrentPlayerToRoom(String roomSecretCode) {
        this.removeCurrentPlayerFromRoom();

        SkyjoRoomDataModel roomDm = skyjoRepository.readRoom(roomSecretCode);
        if (roomDm == null)
            throw new IllegalStateException("Room doesn't exists");

        SkyjoRoomBusinessModel room = SkyjoRoomBusinessModel.fromDataModel(roomDm);
        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        SkyjoRoomMemberBusinessModel member = new SkyjoRoomMemberBusinessModel();
        member.setRoom(room);
        member.setPlayer(me);

        skyjoRepository.createRoomMember(member.toDataModel());
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new PlayerJoinedWebsocketModel(me.getId(), me.getDisplayName()));

        return room;
    }

    @Override
    public void removeCurrentPlayerFromRoom() {
        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        SkyjoRoomMemberDataModel member = skyjoRepository.readRoomMember(me.getId());
        if (member == null)
            return;

        skyjoRepository.deleteRoomMember(member.getRoomId(), member.getPlayerId());
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + member.getRoomId(), new PlayerLeaveWebsocketModel(me.getId()));
    }

    @Override
    public void setCurrentPlayerRoomDisplayName(String displayName) {
        SkyjoPlayerBusinessModel me = getCurrentPlayer();
        SkyjoRoomMemberDataModel member = skyjoRepository.readRoomMember(me.getId());

        if (member == null)
            throw new IllegalStateException("Not in a room");

        SkyjoRoomBusinessModel room = getRoom(member.getRoomId());

        if (!room.getOwnerId().equals(me.getId()))
            throw new IllegalStateException("Not owner of the room");

        room.setDisplayName(displayName);
        skyjoRepository.updateRoom(room.toDataModel());
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new RoomNameChangedWebsocketModel(displayName));
    }

    //#region PlayerActionHandlers
    private void handlePlayerActionDrawACard(SkyjoRoomBusinessModel room) {
        if (room.getStatus() != ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS || room.getCurrentTurnLastAction() != null)
            throw new IllegalStateException("Illegal action");

        Integer topCard = room.getPristineCards().remove(0);
        room.setCurrentDrawnCard(topCard);
        room.setCurrentTurnLastAction(ESkyjoGameActionTypeBusinessModel.DRAW_A_CARD);

        skyjoRepository.updateRoom(room.toDataModel());
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewCurrentDrawnCardWebsocketModel(topCard));
    }

    private void handlePlayerActionExchangeWithPickedCard(SkyjoGameActionBusinessModel action, SkyjoRoomBusinessModel room) {
        if (room.getCurrentTurnLastAction() != ESkyjoGameActionTypeBusinessModel.DRAW_A_CARD)
            throw new IllegalStateException("Illegal action");

        if (action.getCardIndex() == null || action.getCardIndex() < 0 || action.getCardIndex() > 11)
            throw new IllegalStateException("Invalid card index");

        UUID myId = getCurrentPlayer().getId();
        SkyjoRoomMemberBusinessModel me = room.getMembers().stream().filter(member -> member.getPlayer().getId().equals(myId)).findFirst().orElse(null);
        if (me == null)
            throw new IllegalStateException("Not a member of the room");

        Integer cardToAdd = room.getCurrentDrawnCard();
        if (cardToAdd == null)
            throw new IllegalStateException("There is no drawn card");

        Integer cardToDiscard = me.getRealBoard().get(action.getCardIndex());
        if (cardToDiscard.intValue() == DELETED_CARD.intValue())
            throw new IllegalStateException("Cannot exchange a card that do not exists anymore");

        me.getRealBoard().set(action.getCardIndex(), cardToAdd);
        me.getShownBoard()[action.getCardIndex()] = true;
        room.setCurrentDrawnCard(null);
        room.setLastDiscardedCard(cardToDiscard);

        // Room will be updated in #nextPlayerTurn()
        skyjoRepository.updateRoomMember(me.toDataModel());

        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new SetPlayerCardWebsocketModel(myId, action.getCardIndex(), cardToAdd));
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewCurrentDrawnCardWebsocketModel(null));
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewDiscardedCardWebsocketModel(cardToDiscard));

        nextPlayerTurn(room, false);
    }

    private void handlePlayerActionIgnorePickedCard(SkyjoRoomBusinessModel room) {
        if (room.getCurrentTurnLastAction() != ESkyjoGameActionTypeBusinessModel.DRAW_A_CARD)
            throw new IllegalStateException("Illegal action");

        if (room.getCurrentDrawnCard() == null)
            throw new IllegalStateException("No drawn card");

        room.setLastDiscardedCard(room.getCurrentDrawnCard());
        room.setCurrentDrawnCard(null);

        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewCurrentDrawnCardWebsocketModel(null));

        nextPlayerTurn(room, false);
    }

    private void handlePlayerActionExchangeWithDiscardedCard(SkyjoGameActionBusinessModel action, SkyjoRoomBusinessModel room) {
        if (room.getStatus() != ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS || room.getCurrentTurnLastAction() != null)
            throw new IllegalStateException("Illegal action");

        if (action.getCardIndex() == null || action.getCardIndex() < 0 || action.getCardIndex() > 11)
            throw new IllegalStateException("Invalid card index");

        UUID myId = getCurrentPlayer().getId();
        SkyjoRoomMemberBusinessModel me = room.getMembers().stream().filter(member -> member.getPlayer().getId().equals(myId)).findFirst().orElse(null);
        if (me == null)
            throw new IllegalStateException("Not a member of the room");

        Integer cardToDiscard = me.getRealBoard().get(action.getCardIndex());
        if (cardToDiscard.intValue() == DELETED_CARD.intValue())
            throw new IllegalStateException("Cannot exchange a card that do not exists anymore");

        Integer cardToAdd = room.getLastDiscardedCard();

        me.getRealBoard().set(action.getCardIndex(), cardToAdd);
        me.getShownBoard()[action.getCardIndex()] = true;
        room.setLastDiscardedCard(cardToDiscard);

        // Room will be updated in #nextPlayerTurn()
        skyjoRepository.updateRoomMember(me.toDataModel());

        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new SetPlayerCardWebsocketModel(myId, action.getCardIndex(), cardToAdd));

        nextPlayerTurn(room, false);
    }

    private void handlePlayerActionFlipACard(SkyjoGameActionBusinessModel action, SkyjoRoomBusinessModel room) {
        if (
                (room.getStatus() != ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS && room.getStatus() != ESkyjoRoomStatusBusinessModel.SELECTING_CARDS)
                || room.getCurrentTurnLastAction() != null)
            throw new IllegalStateException("Illegal action");

        if (action.getCardIndex() == null || action.getCardIndex() < 0 || action.getCardIndex() > 11)
            throw new IllegalStateException("Invalid card index");

        UUID myId = getCurrentPlayer().getId();
        SkyjoRoomMemberBusinessModel me = room.getMembers().stream().filter(member -> member.getPlayer().getId().equals(myId)).findFirst().orElse(null);
        if (me == null)
            throw new IllegalStateException("Not a member of the room");

        if (me.getShownBoard()[action.getCardIndex()])
            throw new IllegalStateException("Card already flipped");

        me.getShownBoard()[action.getCardIndex()] = true;

        skyjoRepository.updateRoomMember(me.toDataModel());

        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new SetPlayerCardWebsocketModel(myId, action.getCardIndex(), me.getRealBoard().get(action.getCardIndex())));

        if (room.getStatus() == ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS)
            nextPlayerTurn(room, false);
    }
    //#endregion

    @Override
    public void currentPlayerPlayAction(SkyjoGameActionBusinessModel action) {
        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        SkyjoRoomMemberDataModel member = skyjoRepository.readRoomMember(me.getId());
        if (member == null)
            throw new IllegalStateException("Current player is not in a room");

        SkyjoRoomBusinessModel room = getRoom(member.getRoomId());
        if (room.getStatus() == ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS && !room.getCurrentTurnPlayerId().equals(me.getId()))
            throw new IllegalStateException("It's not your turn");

        switch (action.getType()) {
            case DRAW_A_CARD -> this.handlePlayerActionDrawACard(room);
            case EXCHANGE_WITH_PICKED_CARD -> this.handlePlayerActionExchangeWithPickedCard(action, room);
            case IGNORE_PICKED_CARD -> this.handlePlayerActionIgnorePickedCard(room);
            case EXCHANGE_WITH_DISCARDED_CARD -> this.handlePlayerActionExchangeWithDiscardedCard(action, room);
            case FLIP_A_CARD -> this.handlePlayerActionFlipACard(action, room);
            default -> throw new IllegalStateException("Action unknown");
        }
    }
    //#endregion

    //#region GameEngine
    private static final long COUNTDOWN_BEFORE_START = 10_000;
    private static final long TIME_BETWEEN_GAME_START_AND_PLAYER_TURN = 5_000;
    private static final long TIME_PER_PLAYER = 30_000;

    private void nextPlayerTurn(SkyjoRoomBusinessModel room, boolean timeoutPreviousPlayer) {
        int currentPlayerIndex = -1;
        for (int i = 0; i < room.getMembers().size(); i++) {
            SkyjoRoomMemberBusinessModel member = room.getMembers().get(i);
            if (member.getPlayer().getId().equals(room.getCurrentTurnPlayerId())) {
                currentPlayerIndex = i;

                //#region Delete full columns
                // Delete columns if necessary
                int[][] columnsIndexes = {
                        {0, 4, 8},
                        {1, 5, 9},
                        {2, 6, 10},
                        {3, 7, 11}
                };

                for (int[] columnIndexes : columnsIndexes) {
                    // Check that all cards fo the column are shown
                    if (!member.getShownBoard()[columnIndexes[0]]
                            || !member.getShownBoard()[columnIndexes[1]]
                            || !member.getShownBoard()[columnIndexes[2]])
                        continue;

                    int value1 = member.getRealBoard().get(columnIndexes[0]);
                    int value2 = member.getRealBoard().get(columnIndexes[1]);
                    int value3 = member.getRealBoard().get(columnIndexes[2]);

                    // Ignore already deleted column
                    if (value1 == DELETED_CARD)
                        continue;

                    // If all the cards of the column are the same, delete them
                    if (value1 == value2 && value2 == value3) {
                        member.getRealBoard().set(columnIndexes[0], DELETED_CARD);
                        member.getRealBoard().set(columnIndexes[1], DELETED_CARD);
                        member.getRealBoard().set(columnIndexes[2], DELETED_CARD);

                        skyjoRepository.updateRoomMember(member.toDataModel());
                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new SetPlayerCardWebsocketModel(member.getPlayer().getId(), columnIndexes[0], DELETED_CARD));
                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new SetPlayerCardWebsocketModel(member.getPlayer().getId(), columnIndexes[1], DELETED_CARD));
                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new SetPlayerCardWebsocketModel(member.getPlayer().getId(), columnIndexes[2], DELETED_CARD));
                    }
                }
                //#endregion
                break;
            }
        }

        if (currentPlayerIndex == -1)
            throw new IllegalStateException("Player not member of the room anymore");

        if (room.getCurrentDrawnCard() != null) {
            room.setLastDiscardedCard(room.getCurrentDrawnCard());
            room.setCurrentDrawnCard(null);

            simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewCurrentDrawnCardWebsocketModel(null));
        }
        else if (timeoutPreviousPlayer) {
            Integer topCard = room.getPristineCards().remove(0);
            room.setLastDiscardedCard(topCard);
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
        }

        int newCurrentPlayerIndex = (currentPlayerIndex + 1) % room.getMembers().size();
        UUID newPlayerId = room.getMembers().get(newCurrentPlayerIndex).getPlayer().getId();

        room.setCurrentTurnPlayerId(newPlayerId);
        room.setCurrentTurnEndAt(System.currentTimeMillis() + TIME_PER_PLAYER);

        skyjoRepository.updateRoom(room.toDataModel());
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.getId(),
                new NewPlayerTurnWebsocketModel(newPlayerId, room.getCurrentTurnEndAt(), timeoutPreviousPlayer));
    }

    private void runGameEngine(UUID roomId) {
        logger.info("Starting daemon for room " + roomId);

        Callable<List<Integer>> generateRandomCards = () -> {
            List<Integer> cards = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                for (int cardNumber = -2; cardNumber <= 12; cardNumber++)
                    if (
                            (cardNumber == 0) || // 15 cards of 0
                            (cardNumber == -2 && i < 5) || // 5 cards of -2
                            ((cardNumber == -1 || cardNumber >= 1) && i < 10) // 10 cards of -1 and [1; 12]
                    )
                        cards.add(cardNumber);
            }

            if (cards.size() != 150)
                throw new IllegalStateException("Invalid amount of cards after generation");

            Collections.shuffle(cards);
            return cards;
        };
        
        try {
            gameLoop: while (true) {
                SkyjoRoomBusinessModel room = getRoom(roomId);

                if (room.getMembers().isEmpty()) {
                    skyjoRepository.deleteRoom(roomId);
                    simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, new GameInterruptedWebsocketModel());
                    break gameLoop;
                }
                
                if (room.getStatus() == ESkyjoRoomStatusBusinessModel.WAITING_FOR_PLAYERS && room.getMembers().size() >= 2) {
                    if (room.getGameBeginAt() == 0) {
                        room.setGameBeginAt(System.currentTimeMillis() + COUNTDOWN_BEFORE_START);
                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, new GameCountdownStartedWebsocketModel(room.getGameBeginAt()));
                        skyjoRepository.updateRoom(room.toDataModel());
                    }
                    else if (System.currentTimeMillis() >= room.getGameBeginAt()) {
                        room.setStatus(ESkyjoRoomStatusBusinessModel.SELECTING_CARDS);
                        room.setCurrentRound(0);
                        room.setPristineCards(generateRandomCards.call());

                        for (SkyjoRoomMemberBusinessModel member : room.getMembers()) {
                            List<Integer> cards = room.getPristineCards().subList(0, 12);
                            member.setRealBoard(new ArrayList<>(cards));
                            cards.clear();

                            skyjoRepository.updateRoomMember(member.toDataModel());
                        }

                        Integer topCard = room.getPristineCards().remove(0);
                        room.setLastDiscardedCard(topCard);

                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, new SelectingCardsPhaseWebsocketModel());
                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));

                        skyjoRepository.updateRoom(room.toDataModel());
                        logger.info("Skyjo started for room " + roomId);
                        Thread.sleep(TIME_BETWEEN_GAME_START_AND_PLAYER_TURN);
                    }
                }

                else if (room.getStatus() == ESkyjoRoomStatusBusinessModel.SELECTING_CARDS) {
                    boolean letsGo = true;

                    int bestCardsValue = 0;
                    List<UUID> playersWithBestCardsValue = new ArrayList<>();

                    for (SkyjoRoomMemberBusinessModel member : room.getMembers()) {
                        int trueValues = 0;
                        int cardsValue = 0;
                        for (int i = 0; i < 12; i++) {
                            if (member.getShownBoard()[i]) {
                                trueValues++;
                                cardsValue += member.getRealBoard().get(i);
                            }
                        }

                        if (trueValues < 2) {
                            letsGo = false;
                            break;
                        }

                        if (cardsValue > bestCardsValue) {
                            bestCardsValue = cardsValue;
                            playersWithBestCardsValue.clear();
                            playersWithBestCardsValue.add(member.getPlayer().getId());
                        }
                        else if (cardsValue == bestCardsValue) {
                            playersWithBestCardsValue.add(member.getPlayer().getId());
                        }
                    }

                    if (letsGo) {
                        room.setStatus(ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS);

                        UUID chosen = playersWithBestCardsValue.get((int) (Math.random() * playersWithBestCardsValue.size()));
                        room.setCurrentTurnPlayerId(chosen);
                        room.setCurrentTurnEndAt(System.currentTimeMillis() + TIME_PER_PLAYER);

                        skyjoRepository.updateRoom(room.toDataModel());
                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId,
                                new NewPlayerTurnWebsocketModel(chosen, room.getCurrentTurnEndAt(), false));
                    }
                }

                else if (room.getStatus() == ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS) {
                    if (room.getCurrentTurnEndAt() > System.currentTimeMillis()) {
                        //nextPlayerTurn(room, true);
                    }
                }

                Thread.sleep(1000);
            }
        }
        catch (Exception e) {
            skyjoRepository.readRoomMembers(roomId).forEach(member -> skyjoRepository.deleteRoomMember(roomId, member.getPlayerId()));
            skyjoRepository.deleteRoom(roomId);

            simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, new InternalErrorWebsocketModel());
            throw new RuntimeException(e);
        }
        finally {
            logger.info("End of daemon for room " + roomId);
        }
    }
    //#endregion
}
