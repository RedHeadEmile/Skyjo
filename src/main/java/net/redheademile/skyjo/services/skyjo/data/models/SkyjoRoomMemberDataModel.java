package net.redheademile.skyjo.services.skyjo.data.models;

import lombok.*;

import java.util.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SkyjoRoomMemberDataModel {
    private UUID roomId;
    private UUID playerId;

    private List<Integer> realBoard = new ArrayList<>();
    private boolean[] shownBoard = new boolean[12];

    private List<Integer> scores = new ArrayList<>();
}
