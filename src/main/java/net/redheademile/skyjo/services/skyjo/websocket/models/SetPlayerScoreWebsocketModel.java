package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SetPlayerScoreWebsocketModel extends WebsocketModel {
    private UUID playerId;
    private int round;
    private Integer score;

    public SetPlayerScoreWebsocketModel(UUID playerId, int round, Integer score) {
        super("setPlayerScore");
        this.playerId = playerId;
        this.round = round;
        this.score = score;
    }
}
