package net.redheademile.skyjo.services.skyjo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoPlayerBusinessModel;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoRoomBusinessModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoPlayerDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomDataModel;
import net.redheademile.skyjo.services.skyjo.repositories.ISkyjoRepository;
import net.redheademile.skyjo.services.skyjo.websocket.models.PlayerDisplayNameChangedWebsocketModel;
import net.redheademile.skyjo.services.skyjo.websocket.models.PlayerJoinedWebsocketModel;
import net.redheademile.skyjo.services.skyjo.websocket.models.PlayerLeaveWebsocketModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.UUID;

import static net.redheademile.skyjo.utils.Mapping.map;

@Service
@RequestScope
public class SkyjoService implements ISkyjoService {

    private final ISkyjoRepository skyjoRepository;

    private boolean isInitialized = false;
    private SkyjoPlayerBusinessModel currentPlayer;


    private static final String AUTH_COOKIE_NAME = "SkyjoToken";
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public SkyjoService(
            ISkyjoRepository skyjoRepository,

            HttpServletRequest request,
            HttpServletResponse response,

            SimpMessagingTemplate simpMessagingTemplate
    ) {
        this.skyjoRepository = skyjoRepository;
        this.request = request;
        this.response = response;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public SkyjoRoomBusinessModel addRoom(String displayName) {
        UUID uuid;
        String secretCode;
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

        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        SkyjoRoomDataModel newRoom = new SkyjoRoomDataModel();
        newRoom.setId(uuid);
        newRoom.setSecretCode(secretCode);
        newRoom.setDisplayName("My room");
        newRoom.getPlayers().add(skyjoRepository.readPlayer(me.getId()));

        return SkyjoRoomBusinessModel.fromDataModel(newRoom);
    }

    @Override
    public SkyjoRoomBusinessModel getRoom(UUID roomId) {
        SkyjoRoomDataModel dataModel = skyjoRepository.readRoom(roomId);
        if (dataModel == null)
            return null;

        return SkyjoRoomBusinessModel.fromDataModel(dataModel);
    }

    @Override
    public SkyjoRoomBusinessModel getRoom(String roomSecretCode) {
        SkyjoRoomDataModel dataModel = skyjoRepository.readRoom(roomSecretCode);
        if (dataModel == null)
            return null;

        return SkyjoRoomBusinessModel.fromDataModel(dataModel);
    }

    @Override
    public List<SkyjoRoomBusinessModel> getRooms() {
        return map(skyjoRepository.readRooms(), SkyjoRoomBusinessModel::fromDataModel);
    }

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
        }
        catch (IllegalArgumentException ignored) { }

        return newCurrentPlayer();
    }

    @Override
    public SkyjoPlayerBusinessModel setCurrentPlayerName(String displayName) {
        SkyjoPlayerBusinessModel me = getCurrentPlayer();
        me.setDisplayName(displayName);

        SkyjoRoomDataModel room = skyjoRepository.readPlayerRoom(me.getId());
        if (room != null)
            simpMessagingTemplate.convertAndSend("/topic/game/" + room.getId(), new PlayerDisplayNameChangedWebsocketModel(me.getId(), displayName));

        return me;
    }

    @Override
    public SkyjoRoomBusinessModel addCurrentPlayerToRoom(String roomSecretCode) {
        SkyjoRoomDataModel room = skyjoRepository.readRoom(roomSecretCode);
        if (room == null)
            throw new IllegalStateException("Room doesn't exists");

        SkyjoPlayerBusinessModel me = getCurrentPlayer();
        room.getPlayers().add(skyjoRepository.readPlayer(me.getId()));
        simpMessagingTemplate.convertAndSend("/topic/game/" + room.getId(), new PlayerJoinedWebsocketModel(me.getId(), me.getDisplayName()));

        return SkyjoRoomBusinessModel.fromDataModel(room);
    }

    @Override
    public void removeCurrentPlayerFromRoom() {
        SkyjoPlayerBusinessModel me = getCurrentPlayer();

        SkyjoRoomDataModel room = skyjoRepository.readPlayerRoom(me.getId());
        if (room == null)
            return;

        room.getPlayers().removeIf(p -> p.getId().equals(me.getId()));
        simpMessagingTemplate.convertAndSend("/topic/game/" + room.getId(), new PlayerLeaveWebsocketModel(me.getId()));
    }
}
