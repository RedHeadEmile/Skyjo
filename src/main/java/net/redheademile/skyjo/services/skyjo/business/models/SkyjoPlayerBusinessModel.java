package net.redheademile.skyjo.services.skyjo.business.models;

import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoPlayerDataModel;

import java.util.UUID;

@Getter
@Setter
public class SkyjoPlayerBusinessModel {
    private UUID id;
    private String displayName;

    public SkyjoPlayerDataModel toDataModel() {
        return new SkyjoPlayerDataModel() {{
            setId(SkyjoPlayerBusinessModel.this.id);
            setDisplayName(SkyjoPlayerBusinessModel.this.displayName);
        }};
    }

    public static SkyjoPlayerBusinessModel fromDataModel(SkyjoPlayerDataModel dataModel) {
        return new SkyjoPlayerBusinessModel() {{
            setId(dataModel.getId());
            setDisplayName(dataModel.getDisplayName());
        }};
    }
}
