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
    private UUID ownerId;

    private int currentTurn;
    private UUID currentTurnPlayerId;
    private long currentTurnPlayerEndAt;
    private ESkyjoGameActionTypeBusinessModel lastAction;

    private long gameBeginAt;
    private ESkyjoRoomStatusBusinessModel status;

    private List<Integer> pristineCards = new ArrayList<>(150);
    private Integer currentDrawnCard;
    private Integer lastDiscardedCard;

    private List<SkyjoRoomMemberBusinessModel> members = new ArrayList<>();

    public SkyjoRoomDataModel toDataModel() {
        return new SkyjoRoomDataModel() {{
            setId(SkyjoRoomBusinessModel.this.getId());
            setDisplayName(SkyjoRoomBusinessModel.this.getDisplayName());
            setSecretCode(SkyjoRoomBusinessModel.this.getSecretCode());
            setOwnerId(SkyjoRoomBusinessModel.this.getOwnerId());

            setCurrentTurn(SkyjoRoomBusinessModel.this.getCurrentTurn());
            setCurrentTurnPlayerId(SkyjoRoomBusinessModel.this.getCurrentTurnPlayerId());
            setCurrentTurnPlayerEndAt(SkyjoRoomBusinessModel.this.getCurrentTurnPlayerEndAt());
            setLastAction(SkyjoRoomBusinessModel.this.getLastAction() != null ? SkyjoRoomBusinessModel.this.getLastAction().toDataModel() : null);

            setGameBeginAt(SkyjoRoomBusinessModel.this.getGameBeginAt());
            setStatus(SkyjoRoomBusinessModel.this.getStatus().toDataModel());

            setPristineCards(SkyjoRoomBusinessModel.this.getPristineCards());
            setCurrentDrawnCard(SkyjoRoomBusinessModel.this.getCurrentDrawnCard());
            setLastDiscardedCard(SkyjoRoomBusinessModel.this.getLastDiscardedCard());
        }};
    }

    public static SkyjoRoomBusinessModel fromDataModel(SkyjoRoomDataModel dataModel) {
        return new SkyjoRoomBusinessModel() {{
            setId(dataModel.getId());
            setDisplayName(dataModel.getDisplayName());
            setSecretCode(dataModel.getSecretCode());
            setOwnerId(dataModel.getOwnerId());

            setCurrentTurn(dataModel.getCurrentTurn());
            setCurrentTurnPlayerId(dataModel.getCurrentTurnPlayerId());
            setCurrentTurnPlayerEndAt(dataModel.getCurrentTurnPlayerEndAt());
            setLastAction(dataModel.getLastAction() != null ? ESkyjoGameActionTypeBusinessModel.fromDataModel(dataModel.getLastAction()) : null);

            setGameBeginAt(dataModel.getGameBeginAt());
            setStatus(ESkyjoRoomStatusBusinessModel.fromDataModel(dataModel.getStatus()));

            setPristineCards(dataModel.getPristineCards());
            setCurrentDrawnCard(dataModel.getCurrentDrawnCard());
            setLastDiscardedCard(dataModel.getLastDiscardedCard());
        }};
    }
}
