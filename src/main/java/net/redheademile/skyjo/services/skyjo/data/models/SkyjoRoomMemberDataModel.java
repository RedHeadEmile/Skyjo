package net.redheademile.skyjo.services.skyjo.data.models;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class SkyjoRoomMemberDataModel {
    private SkyjoPlayerDataModel player;

    private List<Integer> realBoard = new ArrayList<>();
    private List<Integer> shownBoard = new ArrayList<>();

    private List<Integer> scores = new ArrayList<>();
}
