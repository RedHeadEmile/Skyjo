package net.redheademile.skyjo.services.skyjo.business.models;

import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomDataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.redheademile.skyjo.utils.Mapping.map;

@Getter
@Setter
public class SkyjoRoomBusinessModel {
    private UUID id;
    private String displayName;
    private String secretCode;

    private List<SkyjoPlayerBusinessModel> players = new ArrayList<>();

    public static SkyjoRoomBusinessModel fromDataModel(SkyjoRoomDataModel dataModel) {
        return new SkyjoRoomBusinessModel() {{
            setId(dataModel.getId());
            setDisplayName(dataModel.getDisplayName());
            setSecretCode(dataModel.getSecretCode());
            setPlayers(map(dataModel.getPlayers(), SkyjoPlayerBusinessModel::fromDataModel));
        }};
    }
}
