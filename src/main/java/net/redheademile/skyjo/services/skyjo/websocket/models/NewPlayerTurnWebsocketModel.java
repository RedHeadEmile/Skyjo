package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NewPlayerTurnWebsocketModel extends WebsocketModel {
    private UUID newPlayerId;
    private long newPlayerTurnEndAt;
    private boolean previousPlayerWasTimedOut;

    public NewPlayerTurnWebsocketModel(UUID newPlayerId, long newPlayerTurnEndAt, boolean previousPlayerWasTimedOut) {
        super("newPlayerTurn");
        this.newPlayerId = newPlayerId;
        this.newPlayerTurnEndAt = newPlayerTurnEndAt;
        this.previousPlayerWasTimedOut = previousPlayerWasTimedOut;
    }
}
