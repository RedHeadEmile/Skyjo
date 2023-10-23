package net.redheademile.skyjo.services.skyjo.data.models;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class SkyjoRoomDataModel {
    private UUID id;
    private String displayName;
    private String secretCode;

    private int currentTurn = 1;
    private int currentPlayerTurnIndex = 0;
    private ESkyjoRoomStatusDataModel status = ESkyjoRoomStatusDataModel.WAITING_FOR_PLAYERS;

    private List<Integer> pristineCards = new ArrayList<>(150);
    private List<Integer> discardedCards = new ArrayList<>(150);

    private List<SkyjoPlayerDataModel> players = new ArrayList<>();
}
