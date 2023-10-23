package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoPlayerBusinessModel;

import java.util.UUID;

@Getter
@Setter
public class SkyjoPlayerViewModel {
    @NotBlank
    private UUID id;
    @NotBlank
    @Size(min = 3, max = 32)
    private String displayName;

    public static SkyjoPlayerViewModel fromBusinessModel(SkyjoPlayerBusinessModel businessModel) {
        return new SkyjoPlayerViewModel() {{
            setId(businessModel.getId());
            setDisplayName(businessModel.getDisplayName());
        }};
    }
}
