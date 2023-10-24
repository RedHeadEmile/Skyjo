package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameActionViewModel {
    @NotNull
    private EGameActionTypeViewModel type;
    private Integer cardIndex;
}
