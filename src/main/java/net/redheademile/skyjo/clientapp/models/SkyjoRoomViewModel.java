package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoPlayerBusinessModel;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoRoomBusinessModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.redheademile.skyjo.utils.Mapping.map;

@Getter
@Setter
public class SkyjoRoomViewModel {
    @NotBlank
    private UUID id;
    @NotBlank
    @Size(min = 3, max = 32)
    private String displayName;

    @NotNull
    private List<SkyjoPlayerViewModel> players = new ArrayList<>();

    public static SkyjoRoomViewModel fromBusinessModel(SkyjoRoomBusinessModel businessModel) {
        return new SkyjoRoomViewModel() {{
            setId(businessModel.getId());
            setDisplayName(businessModel.getDisplayName());
            setPlayers(map(businessModel.getPlayers(), SkyjoPlayerViewModel::fromBusinessModel));
        }};
    }
}
