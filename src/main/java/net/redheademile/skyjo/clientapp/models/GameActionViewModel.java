package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.business.models.GameActionBusinessModel;

@Getter
@Setter
public class GameActionViewModel {
    @NotNull
    private EGameActionTypeViewModel type;
    private Integer cardIndex;

    public GameActionBusinessModel toBusinessModel() {
        return new GameActionBusinessModel() {{
            setType(GameActionViewModel.this.getType().toBusinessModel());
            setCardIndex(GameActionViewModel.this.getCardIndex());
        }};
    }
}
