package net.redheademile.skyjo.services.skyjo.business.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkyjoGameActionBusinessModel {
    private ESkyjoGameActionTypeBusinessModel type;
    private Integer cardIndex;
}
