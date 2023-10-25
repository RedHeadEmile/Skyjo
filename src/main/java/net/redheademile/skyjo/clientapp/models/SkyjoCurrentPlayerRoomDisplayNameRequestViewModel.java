package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkyjoCurrentPlayerRoomDisplayNameRequestViewModel {
    @NotBlank
    @Size(min = 3, max = 32)
    private String newDisplayName;
}
