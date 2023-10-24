package net.redheademile.skyjo.services.skyjo.data.models;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SkyjoPlayerDataModel {
    private UUID id;
    private String displayName;
}
