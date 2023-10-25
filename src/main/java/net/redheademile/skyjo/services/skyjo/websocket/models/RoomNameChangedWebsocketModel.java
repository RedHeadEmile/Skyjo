package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomNameChangedWebsocketModel extends WebsocketModel {
    private String newDisplayName;

    public RoomNameChangedWebsocketModel(String newDisplayName) {
        super("roomNameChanged");
        this.newDisplayName = newDisplayName;
    }
}
