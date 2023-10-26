package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DestroyRoomWebsocketModel extends WebsocketModel {
    private UUID roomId;

    public DestroyRoomWebsocketModel(UUID roomId) {
        super("destroyRoom");
        this.roomId = roomId;
    }
}
