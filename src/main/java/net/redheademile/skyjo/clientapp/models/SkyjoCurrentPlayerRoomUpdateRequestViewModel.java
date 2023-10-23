package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkyjoCurrentPlayerRoomUpdateRequestViewModel {
    @NotBlank
    @NotNull
    private String roomSecretCode;
}
