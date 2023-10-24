package net.redheademile.skyjo.services.skyjo.business.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameActionBusinessModel {
    private EGameActionTypeBusinessModel type;
    private Integer cardIndex;
}
