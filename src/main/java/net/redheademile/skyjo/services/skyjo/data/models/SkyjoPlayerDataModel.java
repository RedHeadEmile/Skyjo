package net.redheademile.skyjo.services.skyjo.data.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SkyjoPlayerDataModel {
    private UUID id;
    private String displayName;
}
