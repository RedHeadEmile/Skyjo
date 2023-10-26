package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SetRoomOwnerWebsocketModel extends WebsocketModel {
    private UUID newOwnerId;

    public SetRoomOwnerWebsocketModel(UUID newOwnerId) {
        super("setRoomOwner");
        this.newOwnerId = newOwnerId;
    }
}
