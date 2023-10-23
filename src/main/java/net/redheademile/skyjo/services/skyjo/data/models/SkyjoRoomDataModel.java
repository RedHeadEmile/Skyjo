package net.redheademile.skyjo.services.skyjo.data.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SkyjoRoomDataModel {
    private UUID id;
    private String displayName;
    private String secretCode;

    private List<SkyjoPlayerDataModel> players = new ArrayList<>();
}
