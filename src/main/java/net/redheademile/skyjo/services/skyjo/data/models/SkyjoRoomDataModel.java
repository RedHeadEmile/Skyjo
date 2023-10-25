package net.redheademile.skyjo.services.skyjo.data.models;

import lombok.*;

import java.util.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SkyjoRoomDataModel {
    private UUID id;
    private String displayName;
    private String secretCode;
    private UUID ownerId;

    private int currentTurn;
    private UUID currentTurnPlayerId;
    private long currentTurnPlayerEndAt;
    private ESkyjoGameActionTypeDataModel lastAction;

    private long gameBeginAt;
    private ESkyjoRoomStatusDataModel status = ESkyjoRoomStatusDataModel.WAITING_FOR_PLAYERS;

    private List<Integer> pristineCards = new ArrayList<>(150);
    private Integer currentDrawnCard;
    private Integer lastDiscardedCard;
}
