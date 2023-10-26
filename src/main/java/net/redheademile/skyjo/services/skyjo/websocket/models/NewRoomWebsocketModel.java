package net.redheademile.skyjo.services.skyjo.websocket.models;

import lombok.Getter;
import lombok.Setter;
import net.redheademile.skyjo.clientapp.models.SkyjoRoomViewModel;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoRoomBusinessModel;

@Getter
@Setter
public class NewRoomWebsocketModel extends WebsocketModel {
    private SkyjoRoomViewModel newRoom;

    public NewRoomWebsocketModel(SkyjoRoomBusinessModel newRoom) {
        super("newRoom");
        this.newRoom = SkyjoRoomViewModel.fromBusinessModel(newRoom);
    }
}
