package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameStartedWebsocketModel extends WebsocketModel {
    public GameStartedWebsocketModel() {
        super("gameStarted");
    }
}
