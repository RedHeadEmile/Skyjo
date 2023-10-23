package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class WebsocketModel {
    private String discriminator;

    public WebsocketModel(String discriminator) {
        this.discriminator = discriminator;
    }
}
