package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SetPlayerCardWebsocketModel extends WebsocketModel {
    private UUID playerId;
    private int cardIndex;
    private int cardValue;

    public SetPlayerCardWebsocketModel(UUID playerId, int cardIndex, int cardValue) {
        super("setPlayerCard");
        this.playerId = playerId;
        this.cardIndex = cardIndex;
        this.cardValue = cardValue;
    }
}
