package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GameFinishedWebsocketModel extends WebsocketModel {
    private UUID winnerId;

    public GameFinishedWebsocketModel(UUID winnerId) {
        super("gameFinished");
        this.winnerId = winnerId;
    }
}
