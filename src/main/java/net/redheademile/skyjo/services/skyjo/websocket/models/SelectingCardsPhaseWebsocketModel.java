package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelectingCardsPhaseWebsocketModel extends WebsocketModel {
    public SelectingCardsPhaseWebsocketModel() {
        super("selectingCardsPhase");
    }
}
