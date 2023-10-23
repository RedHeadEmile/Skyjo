package net.redheademile.skyjo.services.skyjo.repositories;

import net.redheademile.skyjo.services.skyjo.data.models.SkyjoPlayerDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomDataModel;

import java.util.List;
import java.util.UUID;

public interface ISkyjoRepository {
    SkyjoRoomDataModel createRoom(SkyjoRoomDataModel room);
    SkyjoRoomDataModel readRoom(UUID roomId);
    SkyjoRoomDataModel readRoom(String roomSecretCode);
    List<SkyjoRoomDataModel> readRooms();
    SkyjoRoomDataModel updateRoom(SkyjoRoomDataModel room);

    SkyjoPlayerDataModel createPlayer(SkyjoPlayerDataModel player);
    SkyjoPlayerDataModel readPlayer(UUID playerId);
    List<SkyjoPlayerDataModel> readPlayers();
    SkyjoPlayerDataModel updatePlayer(SkyjoPlayerDataModel player);

    SkyjoRoomDataModel readPlayerRoom(UUID playerId);
}
