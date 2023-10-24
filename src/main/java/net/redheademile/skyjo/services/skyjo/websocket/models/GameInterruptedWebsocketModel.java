package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameInterruptedWebsocketModel extends WebsocketModel {
    public GameInterruptedWebsocketModel() {
        super("gameInterrupted");
    }
}
