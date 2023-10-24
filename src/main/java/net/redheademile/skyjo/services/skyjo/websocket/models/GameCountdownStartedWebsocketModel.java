package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameCountdownStartedWebsocketModel extends WebsocketModel {
    private long gameBeginAt;

    public GameCountdownStartedWebsocketModel(long gameBeginAt) {
        super("gameCountdownStarted");
        this.gameBeginAt = gameBeginAt;
    }
}
