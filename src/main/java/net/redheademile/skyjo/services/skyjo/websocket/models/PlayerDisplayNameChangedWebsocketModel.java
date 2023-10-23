package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PlayerDisplayNameChangedWebsocketModel extends WebsocketModel {
    private UUID playerId;
    private String newDisplayName;

    public PlayerDisplayNameChangedWebsocketModel(UUID playerId, String newDisplayName) {
        super("playerDisplayNameChanged");
        this.playerId = playerId;
        this.newDisplayName = newDisplayName;
    }
}
