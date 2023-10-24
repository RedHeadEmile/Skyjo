package net.redheademile.skyjo.services.skyjo.business.models;

import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomMemberDataModel;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SkyjoRoomMemberBusinessModel {
    private SkyjoRoomBusinessModel room;
    private SkyjoPlayerBusinessModel player;

    private List<Integer> realBoard = new ArrayList<>();
    private boolean[] shownBoard = new boolean[12];

    private List<Integer> scores = new ArrayList<>();

    public SkyjoRoomMemberDataModel toDataModel() {
        return new SkyjoRoomMemberDataModel() {{
           setRoomId(SkyjoRoomMemberBusinessModel.this.getRoom().getId());
           setPlayerId(SkyjoRoomMemberBusinessModel.this.getPlayer().getId());

           setRealBoard(SkyjoRoomMemberBusinessModel.this.getRealBoard());
           setShownBoard(SkyjoRoomMemberBusinessModel.this.getShownBoard());

           setScores(SkyjoRoomMemberBusinessModel.this.getScores());
        }};
    }

    public static SkyjoRoomMemberBusinessModel fromDataModel(SkyjoRoomMemberDataModel dataModel) {
        return new SkyjoRoomMemberBusinessModel() {{
            setRealBoard(dataModel.getRealBoard());
            setShownBoard(dataModel.getShownBoard());

            setScores(dataModel.getScores());
        }};
    }
}
