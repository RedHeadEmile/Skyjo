package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoRoomBusinessModel;

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
    @NotBlank
    private String secretCode;
    @NotBlank
    @NotNull
    private UUID ownerId;

    @NotNull
    private int currentRound;
    private UUID currentTurnPlayerId;
    @NotNull
    private long currentTurnEndAt;

    @NotNull
    private long gameBeginAt;
    private UUID winnerId;
    @NotNull
    private ESkyjoRoomStatusViewModel status;

    private Integer currentDrawnCard;
    private Integer lastDiscardedCard;

    @NotNull
    private List<SkyjoRoomMemberViewModel> members;

    public static SkyjoRoomViewModel fromBusinessModel(SkyjoRoomBusinessModel businessModel) {
        return new SkyjoRoomViewModel() {{
            setId(businessModel.getId());
            setDisplayName(businessModel.getDisplayName());
            setSecretCode(businessModel.getSecretCode());
            setOwnerId(businessModel.getOwnerId());

            setCurrentRound(businessModel.getCurrentRound());
            setCurrentTurnPlayerId(businessModel.getCurrentTurnPlayerId());
            setCurrentTurnEndAt(businessModel.getCurrentTurnEndAt());

            setGameBeginAt(businessModel.getGameBeginAt());
            setWinnerId(businessModel.getWinnerId());
            setStatus(ESkyjoRoomStatusViewModel.fromBusinessModel(businessModel.getStatus()));

            setCurrentDrawnCard(businessModel.getCurrentDrawnCard());
            setLastDiscardedCard(businessModel.getLastDiscardedCard());

            setMembers(map(businessModel.getMembers(), SkyjoRoomMemberViewModel::fromBusinessModel));
        }};
    }
}
