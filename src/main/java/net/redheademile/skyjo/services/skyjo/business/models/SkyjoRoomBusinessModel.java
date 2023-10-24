package net.redheademile.skyjo.services.skyjo.business.models;

import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomDataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SkyjoRoomBusinessModel {
    private UUID id;
    private String displayName;
    private String secretCode;

    private int currentTurn;
    private UUID currentTurnPlayerId;
    private long currentTurnPlayerEndAt;

    private long gameBeginAt;
    private ESkyjoRoomStatusBusinessModel status;

    private List<Integer> pristineCards = new ArrayList<>(150);
    private List<Integer> discardedCards = new ArrayList<>(150);

    private List<SkyjoRoomMemberBusinessModel> members = new ArrayList<>();

    public SkyjoRoomDataModel toDataModel() {
        return new SkyjoRoomDataModel() {{
            setId(SkyjoRoomBusinessModel.this.getId());
            setDisplayName(SkyjoRoomBusinessModel.this.getDisplayName());
            setSecretCode(SkyjoRoomBusinessModel.this.getSecretCode());

            setCurrentTurn(SkyjoRoomBusinessModel.this.getCurrentTurn());
            setCurrentTurnPlayerId(SkyjoRoomBusinessModel.this.getCurrentTurnPlayerId());
            setCurrentTurnPlayerEndAt(SkyjoRoomBusinessModel.this.getCurrentTurnPlayerEndAt());

            setGameBeginAt(SkyjoRoomBusinessModel.this.getGameBeginAt());
            setStatus(SkyjoRoomBusinessModel.this.getStatus().toDataModel());

            setPristineCards(SkyjoRoomBusinessModel.this.getPristineCards());
            setDiscardedCards(SkyjoRoomBusinessModel.this.getDiscardedCards());
        }};
    }

    public static SkyjoRoomBusinessModel fromDataModel(SkyjoRoomDataModel dataModel) {
        return new SkyjoRoomBusinessModel() {{
            setId(dataModel.getId());
            setDisplayName(dataModel.getDisplayName());
            setSecretCode(dataModel.getSecretCode());

            setCurrentTurn(dataModel.getCurrentTurn());
            setCurrentTurnPlayerId(dataModel.getCurrentTurnPlayerId());
            setCurrentTurnPlayerEndAt(dataModel.getCurrentTurnPlayerEndAt());

            setGameBeginAt(dataModel.getGameBeginAt());
            setStatus(ESkyjoRoomStatusBusinessModel.fromDataModel(dataModel.getStatus()));

            setPristineCards(dataModel.getPristineCards());
            setDiscardedCards(dataModel.getDiscardedCards());
        }};
    }
}
