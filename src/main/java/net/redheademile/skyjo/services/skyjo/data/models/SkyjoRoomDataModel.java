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

    private int currentRound;
    private UUID currentTurnPlayerId;
    private long currentTurnEndAt;
    private ESkyjoGameActionTypeDataModel currentTurnLastAction;

    private long gameBeginAt;
    private UUID winnerId;
    private ESkyjoRoomStatusDataModel status = ESkyjoRoomStatusDataModel.WAITING_FOR_PLAYERS;

    private List<Integer> pristineCards = new ArrayList<>(150);
    private Integer currentDrawnCard;
    private Integer lastDiscardedCard;
}
