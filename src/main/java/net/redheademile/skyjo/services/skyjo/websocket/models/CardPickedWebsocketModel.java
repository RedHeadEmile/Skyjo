package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardPickedWebsocketModel extends WebsocketModel {
    private int cardValue;

    public CardPickedWebsocketModel(int cardValue) {
        super("cardPicked");
        this.cardValue = cardValue;
    }
}
