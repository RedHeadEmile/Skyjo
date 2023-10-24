package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalErrorWebsocketModel extends WebsocketModel {
    public InternalErrorWebsocketModel() {
        super("internalError");
    }
}
