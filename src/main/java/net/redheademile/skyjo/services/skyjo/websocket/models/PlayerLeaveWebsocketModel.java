package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PlayerLeaveWebsocketModel extends WebsocketModel {
    private UUID playerId;

    public PlayerLeaveWebsocketModel(UUID playerId) {
        super("playerLeave");
        this.playerId = playerId;
    }
}
