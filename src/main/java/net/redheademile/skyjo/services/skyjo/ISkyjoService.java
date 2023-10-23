package net.redheademile.skyjo.services.skyjo;

import net.redheademile.skyjo.services.skyjo.business.models.SkyjoPlayerBusinessModel;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoRoomBusinessModel;

import java.util.List;
import java.util.UUID;

public interface ISkyjoService {
    SkyjoRoomBusinessModel addRoom(String displayName);
    SkyjoRoomBusinessModel getRoom(UUID roomId);
    SkyjoRoomBusinessModel getRoom(String roomSecretCode);
    List<SkyjoRoomBusinessModel> getRooms();

    SkyjoPlayerBusinessModel getCurrentPlayer();
    SkyjoPlayerBusinessModel setCurrentPlayerName(String displayName);
    SkyjoRoomBusinessModel addCurrentPlayerToRoom(String roomSecretCode);
    void removeCurrentPlayerFromRoom();
}
