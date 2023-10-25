package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDiscardedCardWebsocketModel extends WebsocketModel {
    private int newDiscardedCardValue;

    public NewDiscardedCardWebsocketModel(int newDiscardedCardValue) {
        super("newDiscardedCard");
        this.newDiscardedCardValue = newDiscardedCardValue;
    }
}
