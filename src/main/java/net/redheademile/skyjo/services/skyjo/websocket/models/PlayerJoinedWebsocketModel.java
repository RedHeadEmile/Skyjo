package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PlayerJoinedWebsocketModel extends WebsocketModel {
    private UUID playerId;
    private String displayName;

    public PlayerJoinedWebsocketModel(UUID playerId, String displayName) {
        super("playerJoined");
        this.playerId = playerId;
        this.displayName = displayName;
    }
}
