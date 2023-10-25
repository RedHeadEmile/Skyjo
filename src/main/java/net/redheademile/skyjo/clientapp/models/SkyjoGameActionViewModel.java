package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoGameActionBusinessModel;

@Getter
@Setter
public class SkyjoGameActionViewModel {
    @NotNull
    private ESkyjoGameActionTypeViewModel type;
    private Integer cardIndex;

    public SkyjoGameActionBusinessModel toBusinessModel() {
        return new SkyjoGameActionBusinessModel() {{
            setType(SkyjoGameActionViewModel.this.getType().toBusinessModel());
            setCardIndex(SkyjoGameActionViewModel.this.getCardIndex());
        }};
    }
}
