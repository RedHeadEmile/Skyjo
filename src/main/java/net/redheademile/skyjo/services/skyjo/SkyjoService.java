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
import org.apache.commons.lang3.NotImplementedException;
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

    private void handlePlayerActionDrawACard() {

    }

    private void handlePlayerActionExchangeWithPickedCard(SkyjoGameActionBusinessModel action) {

    }

    private void handlePlayerActionIgnorePickedCard() {

    }

    private void handlePlayerActionExchangeWithDiscardedCard(SkyjoGameActionBusinessModel action) {

    }

    private void handlePlayerActionFlipACard(SkyjoGameActionBusinessModel action) {

    }

    @Override
    public void currentPlayerExecute(SkyjoGameActionBusinessModel action) {
        switch (action.getType()) {
            case DRAW_A_CARD -> this.handlePlayerActionDrawACard();
            case EXCHANGE_WITH_PICKED_CARD -> this.handlePlayerActionExchangeWithPickedCard(action);
            case IGNORE_PICKED_CARD -> this.handlePlayerActionIgnorePickedCard();
            case EXCHANGE_WITH_DISCARDED_CARD -> this.handlePlayerActionExchangeWithDiscardedCard(action);
            case FLIP_A_CARD -> this.handlePlayerActionFlipACard(action);
        }

        throw new NotImplementedException();
    }

    //#endregion

    //#region GameEngine
    private void runGameEngine(UUID roomId) {
        final long countdownBeforeStart = 10_000;
        final long timePerPlayer = 30_000;
        final long timeBetweenGameStartAndPlayerTurn = 5_000;

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
                        room.setGameBeginAt(System.currentTimeMillis() + countdownBeforeStart);
                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, new GameCountdownStartedWebsocketModel(room.getGameBeginAt()));
                        skyjoRepository.updateRoom(room.toDataModel());
                    }
                    else if (System.currentTimeMillis() >= room.getGameBeginAt()) {
                        room.setStatus(ESkyjoRoomStatusBusinessModel.SELECTING_CARDS_PHASE);
                        room.setCurrentTurn(0);
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
                        Thread.sleep(timeBetweenGameStartAndPlayerTurn);
                    }
                }

                else if (room.getStatus() == ESkyjoRoomStatusBusinessModel.SELECTING_CARDS_PHASE) {
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
                        room.setStatus(ESkyjoRoomStatusBusinessModel.TURNS_IN_PROGRESS);

                        UUID chosen = playersWithBestCardsValue.get((int) (Math.random() * playersWithBestCardsValue.size()));
                        room.setCurrentTurnPlayerId(chosen);
                        room.setCurrentTurnPlayerEndAt(System.currentTimeMillis() + timePerPlayer);

                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId,
                                new NewPlayerTurnWebsocketModel(chosen, room.getCurrentTurnPlayerEndAt(), false));
                    }
                }

                else if (room.getStatus() == ESkyjoRoomStatusBusinessModel.TURNS_IN_PROGRESS) {
                    if (room.getCurrentTurnPlayerEndAt() > System.currentTimeMillis()) {
                        int currentPlayerIndex = -1;
                        for (int i = 0; i < room.getMembers().size(); i++)
                            if (room.getMembers().get(i).getPlayer().getId().equals(room.getCurrentTurnPlayerId())) {
                                currentPlayerIndex = i;
                                break;
                            }

                        if (currentPlayerIndex == -1)
                            throw new IllegalStateException("Player not member of the room anymore");

                        int newCurrentPlayerIndex = (currentPlayerIndex + 1) % room.getMembers().size();
                        UUID newPlayerId = room.getMembers().get(newCurrentPlayerIndex).getPlayer().getId();

                        room.setCurrentTurnPlayerId(newPlayerId);
                        room.setCurrentTurnPlayerEndAt(System.currentTimeMillis() + timePerPlayer);
                        simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId,
                                new NewPlayerTurnWebsocketModel(newPlayerId, room.getCurrentTurnPlayerEndAt(), true));
                    }
                }

                Thread.sleep(1000);
            }
        }
        catch (Exception e) {
            simpMessagingTemplate.convertAndSend("/topic/rooms/" + roomId, new InternalErrorWebsocketModel());
            throw new RuntimeException(e);
        }
        finally {
            logger.info("End of daemon for room " + roomId);
        }
    }
    //#endregion
}
