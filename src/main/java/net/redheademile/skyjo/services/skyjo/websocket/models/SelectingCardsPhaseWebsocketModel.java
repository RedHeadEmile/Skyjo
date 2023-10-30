package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelectingCardsPhaseWebsocketModel extends WebsocketModel {
    private int roundNumber;
    public SelectingCardsPhaseWebsocketModel(int roundNumber) {
        super("selectingCardsPhase");
        this.roundNumber = roundNumber;
    }
}
