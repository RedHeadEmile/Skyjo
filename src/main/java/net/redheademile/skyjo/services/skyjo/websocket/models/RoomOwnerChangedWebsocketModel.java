package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoomOwnerChangedWebsocketModel extends WebsocketModel {
    private UUID newOwnerId;

    public RoomOwnerChangedWebsocketModel(UUID newOwnerId) {
        super("roomOwnerChanged");
        this.newOwnerId = newOwnerId;
    }
}
