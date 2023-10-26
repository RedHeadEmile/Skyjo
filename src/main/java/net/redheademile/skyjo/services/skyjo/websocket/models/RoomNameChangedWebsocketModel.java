package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoomNameChangedWebsocketModel extends WebsocketModel {
    private UUID roomId;
    private String newDisplayName;

    public RoomNameChangedWebsocketModel(UUID roomId, String newDisplayName) {
        super("roomNameChanged");
        this.roomId = roomId;
        this.newDisplayName = newDisplayName;
    }
}
