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
    private static final Integer UNKNOWN_SCORE = -100;
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

    //#region Websocket
    private void sendRoomWebsocket(UUID roomId, WebsocketModel payload) {
        if (roomId == null)
            simpMessagingTemplate.convertAndSend("/topic/rooms", payload);
        else
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, payload);
    }
    //#endregion

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
        //#endregion

        //#region Add currentPlayer to the room
        removeCurrentPlayerFromRoom();

        SkyjoRoomMemberBusinessModel member = new SkyjoRoomMemberBusinessModel();
        member.setRoom(newRoom);
        member.setPlayer(me);
        newRoom.getMembers().add(member);
        //#endregion

        skyjoRepository.createRoom(newRoom.toDataModel());
        skyjoRepository.createRoomMember(member.toDataModel());
        sendRoomWebsocket(null, new NewRoomWebsocketModel(newRoom));

        // Start the game engine for this room
        asyncService.runAsync(() -> runRoomDaemon(newRoom.getId()));

        return newRoom;
    }

    private void fillRoomMembers(SkyjoRoomBusinessModel room) {
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
        fillRoomMembers(room);

        return room;
    }

    @Override
    public SkyjoRoomBusinessModel getRoom(String roomSecretCode) {
        SkyjoRoomDataModel dataModel = skyjoRepository.readRoom(roomSecretCode);
        if (dataModel == null)
            return null;

        SkyjoRoomBusinessModel room = SkyjoRoomBusinessModel.fromDataModel(dataModel);
        fillRoomMembers(room);

        return room;
    }

    @Override
    public List<SkyjoRoomBusinessModel> getRooms() {
        return map(skyjoRepository.readRooms(), roomDataModel -> {
            SkyjoRoomBusinessModel room = SkyjoRoomBusinessModel.fromDataModel(roomDataModel);
            fillRoomMembers(room);
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
            sendRoomWebsocket(member.getRoomId(), new PlayerDisplayNameChangedWebsocketModel(me.getId(), displayName));

        return me;
    }

    @Override
    public SkyjoRoomBusinessModel addCurrentPlayerToRoom(String roomSecretCode) {
        this.removeCurrentPlayerFromRoom();

        SkyjoRoomBusinessModel room = getRoom(roomSecretCode);
        if (room == null)
            throw new IllegalStateException("Room doesn't exists");

        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        if (room.getStatus() != ESkyjoRoomStatusBusinessModel.WAITING_FOR_PLAYERS)
            throw new IllegalStateException("You cannot join a started room");

        SkyjoRoomMemberBusinessModel member = new SkyjoRoomMemberBusinessModel();
        member.setRoom(room);
        member.setPlayer(me);

        skyjoRepository.createRoomMember(member.toDataModel());
        sendRoomWebsocket(room.getId(), new PlayerJoinedWebsocketModel(me.getId(), me.getDisplayName()));

        // Try to start the game if there is enough players
        if (room.getMembers().size() + 1 >= 2 && room.getGameBeginAt() == 0) {
            room.setGameBeginAt(System.currentTimeMillis() + COUNTDOWN_BEFORE_START);
            skyjoRepository.updateRoom(room.toDataModel());
            sendRoomWebsocket(room.getId(), new GameCountdownStartedWebsocketModel(room.getGameBeginAt()));
        }

        return room;
    }

    @Override
    public void removeCurrentPlayerFromRoom() {
        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        SkyjoRoomMemberDataModel member = skyjoRepository.readRoomMember(me.getId());
        if (member == null)
            return;

        SkyjoRoomBusinessModel room = getRoom(member.getRoomId());
        if (room.getStatus() != ESkyjoRoomStatusBusinessModel.WAITING_FOR_PLAYERS)
            throw new IllegalStateException("You cannot leave a started game");

        skyjoRepository.deleteRoomMember(member.getRoomId(), member.getPlayerId());
        sendRoomWebsocket(member.getRoomId(), new PlayerLeaveWebsocketModel(me.getId()));

        // Delete the room is empty
        if (room.getMembers().size() - 1 == 0) {
            skyjoRepository.deleteRoom(member.getRoomId());
            sendRoomWebsocket(member.getRoomId(), new GameInterruptedWebsocketModel());
            sendRoomWebsocket(null, new DestroyRoomWebsocketModel(member.getRoomId()));
        }
        else {
            // Stop the countdown if there is not enough players
            if (room.getMembers().size() - 1 < 2 && room.getGameBeginAt() > 0) {
                room.setGameBeginAt(0);
                skyjoRepository.updateRoom(room.toDataModel());
                sendRoomWebsocket(room.getId(), new GameCountdownStartedWebsocketModel(room.getGameBeginAt()));
            }

            // Need to redefine an owner if he leaves
            if (me.getId().equals(room.getOwnerId())) {
                // (first member should be the oldest)
                room.setMembers(room.getMembers().stream().filter(m -> !m.getPlayer().getId().equals(me.getId())).toList());
                SkyjoRoomMemberBusinessModel newOwner = room.getMembers().stream().findFirst().orElse(null);
                if (newOwner == null)
                    throw new IllegalStateException("It can't be...");

                room.setOwnerId(newOwner.getPlayer().getId());

                skyjoRepository.updateRoom(room.toDataModel());
                sendRoomWebsocket(room.getId(), new SetRoomOwnerWebsocketModel(newOwner.getPlayer().getId()));
            }
        }
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

        WebsocketModel websocketPayload = new RoomNameChangedWebsocketModel(room.getId(), displayName);
        sendRoomWebsocket(null, websocketPayload);
        sendRoomWebsocket(room.getId(), websocketPayload);
    }

    //#region PlayerActionHandlers
    private void handlePlayerActionDrawACard(SkyjoRoomBusinessModel room) {
        if (room.getStatus() != ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS || room.getCurrentTurnLastAction() != null)
            throw new IllegalStateException("Illegal action");

        Integer topCard = room.getPristineCards().remove(0);
        room.setCurrentDrawnCard(topCard);
        room.setCurrentTurnLastAction(ESkyjoGameActionTypeBusinessModel.DRAW_A_CARD);

        skyjoRepository.updateRoom(room.toDataModel());
        sendRoomWebsocket(room.getId(), new NewCurrentDrawnCardWebsocketModel(topCard));
    }

    private void handlePlayerActionExchangeWithDrawnCard(SkyjoGameActionBusinessModel action, SkyjoRoomBusinessModel room) {
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

        sendRoomWebsocket(room.getId(), new SetPlayerCardWebsocketModel(myId, action.getCardIndex(), cardToAdd));
        sendRoomWebsocket(room.getId(), new NewCurrentDrawnCardWebsocketModel(null));
        sendRoomWebsocket(room.getId(), new NewDiscardedCardWebsocketModel(cardToDiscard));

        asyncService.runAsync(() -> nextPlayerTurn(room, false));
    }

    private void handlePlayerActionIgnoreDrawnCard(SkyjoRoomBusinessModel room) {
        if (room.getCurrentTurnLastAction() != ESkyjoGameActionTypeBusinessModel.DRAW_A_CARD)
            throw new IllegalStateException("Illegal action");

        if (room.getCurrentDrawnCard() == null)
            throw new IllegalStateException("No drawn card");

        room.setLastDiscardedCard(room.getCurrentDrawnCard());
        room.setCurrentDrawnCard(null);

        sendRoomWebsocket(room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
        sendRoomWebsocket(room.getId(), new NewCurrentDrawnCardWebsocketModel(null));

        asyncService.runAsync(() -> nextPlayerTurn(room, false));
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

        sendRoomWebsocket(room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
        sendRoomWebsocket(room.getId(), new SetPlayerCardWebsocketModel(myId, action.getCardIndex(), cardToAdd));

        asyncService.runAsync(() -> nextPlayerTurn(room, false));
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

        // Check if he doesn't already have flipped 2 cards
        if (room.getStatus() == ESkyjoRoomStatusBusinessModel.SELECTING_CARDS) {
            int shownCards = 0;
            for (boolean b : me.getShownBoard())
                if (b) ++shownCards;

            if (shownCards >= 2)
                throw new IllegalStateException("You can't flip another card yet.");
        }

        me.getShownBoard()[action.getCardIndex()] = true;

        skyjoRepository.updateRoomMember(me.toDataModel());

        sendRoomWebsocket(room.getId(), new SetPlayerCardWebsocketModel(myId, action.getCardIndex(), me.getRealBoard().get(action.getCardIndex())));

        if (room.getStatus() == ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS)
            asyncService.runAsync(() -> nextPlayerTurn(room, false));

        else if (room.getStatus() == ESkyjoRoomStatusBusinessModel.SELECTING_CARDS) {
            boolean letsStartTheFirstTurn = true;

            int bestTotalCardsValue = Integer.MIN_VALUE;
            List<UUID> playersWithBestTotalCardsValue = new ArrayList<>();

            for (SkyjoRoomMemberBusinessModel member : room.getMembers()) {
                int shownCards = 0;
                int totalCardsValue = 0;
                for (int cardIndex = 0; cardIndex < 12; cardIndex++) {
                    if (member.getShownBoard()[cardIndex]) {
                        shownCards++;
                        totalCardsValue += member.getRealBoard().get(cardIndex);
                    }
                }

                if (shownCards < 2) {
                    letsStartTheFirstTurn = false;
                    break;
                }

                if (shownCards > 2)
                    throw new IllegalStateException(member.getPlayer().getDisplayName() + " (" + member.getPlayer().getId() + ") cheated!");

                if (totalCardsValue > bestTotalCardsValue) {
                    bestTotalCardsValue = totalCardsValue;
                    playersWithBestTotalCardsValue.clear();
                    playersWithBestTotalCardsValue.add(member.getPlayer().getId());
                }
                else if (totalCardsValue == bestTotalCardsValue) {
                    playersWithBestTotalCardsValue.add(member.getPlayer().getId());
                }
            }

            if (letsStartTheFirstTurn) {
                room.setStatus(ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS);

                UUID chosenPlayerId = playersWithBestTotalCardsValue.get((int) (Math.random() * playersWithBestTotalCardsValue.size()));
                room.setCurrentTurnPlayerId(chosenPlayerId);
                room.setCurrentTurnEndAt(System.currentTimeMillis() + TIME_PER_PLAYER);

                skyjoRepository.updateRoom(room.toDataModel());
                sendRoomWebsocket(room.getId(), new NewPlayerTurnWebsocketModel(chosenPlayerId, room.getCurrentTurnEndAt(), false));
            }
        }
    }
    //#endregion

    @Override
    public void addCurrentPlayerGameAction(SkyjoGameActionBusinessModel action) {
        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        SkyjoRoomMemberDataModel member = skyjoRepository.readRoomMember(me.getId());
        if (member == null)
            throw new IllegalStateException("Current player is not in a room");

        SkyjoRoomBusinessModel room = getRoom(member.getRoomId());
        if (room.getStatus() == ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS && !room.getCurrentTurnPlayerId().equals(me.getId()))
            throw new IllegalStateException("It's not your turn");

        switch (action.getType()) {
            case DRAW_A_CARD -> this.handlePlayerActionDrawACard(room);
            case EXCHANGE_WITH_DRAWN_CARD -> this.handlePlayerActionExchangeWithDrawnCard(action, room);
            case IGNORE_DRAWN_CARD -> this.handlePlayerActionIgnoreDrawnCard(room);
            case EXCHANGE_WITH_DISCARDED_CARD -> this.handlePlayerActionExchangeWithDiscardedCard(action, room);
            case FLIP_A_CARD -> this.handlePlayerActionFlipACard(action, room);
            default -> throw new IllegalStateException("Unknown action");
        }
    }
    //#endregion

    //#region GameEngine
    private static final long COUNTDOWN_BEFORE_START = 10_000;
    private static final long TIME_PER_PLAYER = 60_000;

    private List<Integer> generateRandomDeck() {
        List<Integer> cards = new ArrayList<>();
        for (int i = 0; i < 15; ++i) {
            for (int cardNumber = -2; cardNumber <= 12; ++cardNumber)
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
    }

    private void startNewRound(int newRoundNumber, SkyjoRoomBusinessModel room) {
        room.setCurrentRound(newRoundNumber);
        room.setStatus(ESkyjoRoomStatusBusinessModel.SELECTING_CARDS);
        room.setPristineCards(generateRandomDeck());

        // Cards distribution
        for (SkyjoRoomMemberBusinessModel member : room.getMembers()) {
            List<Integer> cards = room.getPristineCards().subList(0, 12);
            member.setShownBoard(new boolean[12]);
            member.setRealBoard(new ArrayList<>(cards));
            cards.clear();

            skyjoRepository.updateRoomMember(member.toDataModel());
        }

        // Draw the first card
        Integer topCard = room.getPristineCards().remove(0);
        room.setLastDiscardedCard(topCard);

        sendRoomWebsocket(room.getId(), new SelectingCardsPhaseWebsocketModel(newRoundNumber));
        sendRoomWebsocket(room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
    }

    private void removeFullColumns(UUID roomId, SkyjoRoomMemberBusinessModel member) {
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
                sendRoomWebsocket(roomId, new SetPlayerCardWebsocketModel(member.getPlayer().getId(), columnIndexes[0], DELETED_CARD));
                sendRoomWebsocket(roomId, new SetPlayerCardWebsocketModel(member.getPlayer().getId(), columnIndexes[1], DELETED_CARD));
                sendRoomWebsocket(roomId, new SetPlayerCardWebsocketModel(member.getPlayer().getId(), columnIndexes[2], DELETED_CARD));
                sendRoomWebsocket(roomId, new NewDiscardedCardWebsocketModel(value1));
            }
        }
    }

    private void nextPlayerTurn(SkyjoRoomBusinessModel room, boolean timeoutPreviousPlayer) throws InterruptedException {
        int currentMemberIndex = -1;
        for (int i = 0; i < room.getMembers().size(); i++) {
            SkyjoRoomMemberBusinessModel member = room.getMembers().get(i);
            if (member.getPlayer().getId().equals(room.getCurrentTurnPlayerId())) {
                currentMemberIndex = i;
                removeFullColumns(room.getId(), member);
                break;
            }
        }

        if (currentMemberIndex == -1)
            throw new IllegalStateException("Player not member of the room anymore");

        if (room.getCurrentDrawnCard() != null) {
            if (!timeoutPreviousPlayer)
                throw new IllegalStateException("Why is there a drawn card so ???");

            // If timed out but a card has been drawn, move this card to the discarded ones

            room.setLastDiscardedCard(room.getCurrentDrawnCard());
            room.setCurrentDrawnCard(null);

            sendRoomWebsocket(room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
            sendRoomWebsocket(room.getId(), new NewCurrentDrawnCardWebsocketModel(null));
        }
        else if (timeoutPreviousPlayer) {
            // If timed out, draw a card and move it to the discarded ones

            Integer topCard = room.getPristineCards().remove(0);
            room.setLastDiscardedCard(topCard);
            sendRoomWebsocket(room.getId(), new NewDiscardedCardWebsocketModel(room.getLastDiscardedCard()));
        }

        int newCurrentPlayerIndex = (currentMemberIndex + 1) % room.getMembers().size();
        SkyjoRoomMemberBusinessModel newMember = room.getMembers().get(newCurrentPlayerIndex);

        // Check if the round is over
        boolean newPlayerHaveAllHisCardsShown = true;
        for (boolean b : newMember.getShownBoard())
            if (!b) {
                newPlayerHaveAllHisCardsShown = false;
                break;
            }

        // Round is over
        if (newPlayerHaveAllHisCardsShown) {
            // Flip all the remaining cards
            for (SkyjoRoomMemberBusinessModel member : room.getMembers()) {
                for (int cardIndex = 0; cardIndex < 12; cardIndex++) {
                    if (!member.getShownBoard()[cardIndex]) {
                        member.getShownBoard()[cardIndex] = true;
                        sendRoomWebsocket(room.getId(), new SetPlayerCardWebsocketModel(member.getPlayer().getId(), cardIndex, member.getRealBoard().get(cardIndex)));
                    }
                }
            }

            Thread.sleep(5000);

            // Score calculation
            Integer lowerScore = null;
            Integer scoreOfPlayerWhoEndedTheRound = null;

            boolean gameIsOver = false;
            for (SkyjoRoomMemberBusinessModel member : room.getMembers()) {
                int totalCardsValue = 0;
                for (Integer cardValue : member.getRealBoard())
                    if (cardValue.intValue() != DELETED_CARD.intValue())
                        totalCardsValue += cardValue;

                if (totalCardsValue >= 100)
                    gameIsOver = true;

                if (member.getPlayer().getId().equals(newMember.getPlayer().getId()))
                    scoreOfPlayerWhoEndedTheRound = totalCardsValue;

                if (lowerScore == null || totalCardsValue < lowerScore)
                    lowerScore = totalCardsValue;

                while (member.getScores().size() < room.getCurrentRound()) {
                    member.getScores().add(UNKNOWN_SCORE);
                    sendRoomWebsocket(room.getId(), new SetPlayerScoreWebsocketModel(member.getPlayer().getId(), member.getScores().size() - 1, null));
                }

                member.getScores().add(totalCardsValue);
                sendRoomWebsocket(room.getId(), new SetPlayerScoreWebsocketModel(member.getPlayer().getId(), room.getCurrentRound(), totalCardsValue));
                Thread.sleep(500);

                removeFullColumns(room.getId(), member);

                skyjoRepository.updateRoomMember(member.toDataModel());
            }

            // If the player who ended the round is not the one with the lower score, he gets double points
            if (scoreOfPlayerWhoEndedTheRound == null)
                throw new IllegalStateException("Player who ended the round is not in the room anymore");

            if (scoreOfPlayerWhoEndedTheRound > lowerScore && scoreOfPlayerWhoEndedTheRound > 0) {
                newMember.getScores().set(room.getCurrentRound(), scoreOfPlayerWhoEndedTheRound * 2);
                sendRoomWebsocket(room.getId(), new SetPlayerScoreWebsocketModel(newMember.getPlayer().getId(), room.getCurrentRound(), scoreOfPlayerWhoEndedTheRound * 2));
            }

            if (gameIsOver) {
                UUID winnerId = null;
                int lowerTotalScores = Integer.MAX_VALUE;

                for (SkyjoRoomMemberBusinessModel member : room.getMembers()) {
                    int totalScores = 0;
                    for (Integer score : member.getScores())
                        if (score != null && score.intValue() != UNKNOWN_SCORE.intValue())
                            totalScores += score;

                    if (winnerId == null || totalScores < lowerTotalScores) {
                        lowerTotalScores = totalScores;
                        winnerId = member.getPlayer().getId();
                    }
                }

                if (winnerId == null)
                    throw new IllegalStateException("No winner found");

                room.setStatus(ESkyjoRoomStatusBusinessModel.FINISHED);
                room.setWinnerId(winnerId);
                sendRoomWebsocket(room.getId(), new GameFinishedWebsocketModel(winnerId));
            }
            else {
                startNewRound(room.getCurrentRound() + 1, room);
            }

            room.setCurrentTurnPlayerId(null);
            room.setCurrentTurnEndAt(0);
        }
        else {
            room.setCurrentTurnPlayerId(newMember.getPlayer().getId());
            room.setCurrentTurnEndAt(System.currentTimeMillis() + TIME_PER_PLAYER);

            sendRoomWebsocket(room.getId(), new NewPlayerTurnWebsocketModel(newMember.getPlayer().getId(), room.getCurrentTurnEndAt(), timeoutPreviousPlayer));
        }

        room.setCurrentTurnLastAction(null);

        skyjoRepository.updateRoom(room.toDataModel());
    }

    private void runRoomDaemon(UUID roomId) {
        logger.info("Starting daemon for room " + roomId);
        
        try {
            SkyjoRoomBusinessModel room;

            do {
                room = getRoom(roomId);
                if (room == null)
                    break;
                
                if (
                        room.getStatus() == ESkyjoRoomStatusBusinessModel.WAITING_FOR_PLAYERS
                                && room.getMembers().size() >= 2
                                && room.getGameBeginAt() > 0
                                && System.currentTimeMillis() >= room.getGameBeginAt()
                ) {
                    startNewRound(0, room);

                    skyjoRepository.updateRoom(room.toDataModel());
                    logger.info("Skyjo started for room " + roomId);
                }

                // Player turn timed out
                else if (room.getStatus() == ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS && System.currentTimeMillis() >= room.getCurrentTurnEndAt())
                    nextPlayerTurn(room, true);

                Thread.sleep(1000);
            }
            while(room.getStatus() != ESkyjoRoomStatusBusinessModel.INTERRUPTED
                    && room.getStatus() != ESkyjoRoomStatusBusinessModel.CRASHED
                    && room.getStatus() != ESkyjoRoomStatusBusinessModel.FINISHED);
        }
        catch (Exception e) {
            skyjoRepository.readRoomMembers(roomId).forEach(member -> skyjoRepository.deleteRoomMember(roomId, member.getPlayerId()));
            skyjoRepository.deleteRoom(roomId);

            sendRoomWebsocket(roomId, new InternalErrorWebsocketModel());
            sendRoomWebsocket(null, new DestroyRoomWebsocketModel(roomId));
            throw new RuntimeException(e);
        }
        finally {
            logger.info("End of daemon for room " + roomId);
        }
    }
    //#endregion
}
