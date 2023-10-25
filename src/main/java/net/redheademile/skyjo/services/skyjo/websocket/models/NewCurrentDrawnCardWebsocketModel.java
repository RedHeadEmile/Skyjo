package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCurrentDrawnCardWebsocketModel extends WebsocketModel {
    private Integer newCurrentDrawCardValue;

    public NewCurrentDrawnCardWebsocketModel(Integer newCurrentDrawCardValue) {
        super("newCurrentDrawnCard");
        this.newCurrentDrawCardValue = newCurrentDrawCardValue;
    }
}
