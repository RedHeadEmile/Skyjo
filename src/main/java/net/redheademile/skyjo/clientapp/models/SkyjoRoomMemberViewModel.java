package net.redheademile.skyjo.clientapp.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoRoomMemberBusinessModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SkyjoRoomMemberViewModel {
    @NotNull
    @NotBlank
    private UUID playerId;
    @NotNull
    @NotBlank
    private String playerDisplayName;

    @NotNull
    private List<Integer> board;
    @NotNull
    private List<Integer> scores;

    public static SkyjoRoomMemberViewModel fromBusinessModel(SkyjoRoomMemberBusinessModel businessModel) {
        List<Integer> board = new ArrayList<>(12);
        for (int i = 0; i < 12; i++)
            board.add(businessModel.getShownBoard()[i] ? businessModel.getRealBoard().get(i) : -10);

        return new SkyjoRoomMemberViewModel() {{
            setPlayerId(businessModel.getPlayer().getId());
            setPlayerDisplayName(businessModel.getPlayer().getDisplayName());

            setBoard(board);
            setScores(businessModel.getScores());
        }};
    }
}
