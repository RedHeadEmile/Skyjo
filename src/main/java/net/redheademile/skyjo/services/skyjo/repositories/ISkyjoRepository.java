package net.redheademile.skyjo.services.skyjo.repositories;

import net.redheademile.skyjo.services.skyjo.data.models.SkyjoPlayerDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomMemberDataModel;

import java.util.List;
import java.util.UUID;

public interface ISkyjoRepository {
    //#region Room
    SkyjoRoomDataModel createRoom(SkyjoRoomDataModel room);
    SkyjoRoomDataModel readRoom(UUID roomId);
    SkyjoRoomDataModel readRoom(String roomSecretCode);
    List<SkyjoRoomDataModel> readRooms();
    SkyjoRoomDataModel updateRoom(SkyjoRoomDataModel room);
    boolean deleteRoom(UUID roomId);
    //#endregion

    //#region Player
    SkyjoPlayerDataModel createPlayer(SkyjoPlayerDataModel player);
    SkyjoPlayerDataModel readPlayer(UUID playerId);
    List<SkyjoPlayerDataModel> readPlayers();
    SkyjoPlayerDataModel updatePlayer(SkyjoPlayerDataModel player);
    boolean deletePlayer(UUID playerId);
    //#endregion

    //#region RoomMember
    SkyjoRoomMemberDataModel createRoomMember(SkyjoRoomMemberDataModel roomMember);
    SkyjoRoomMemberDataModel readRoomMember(UUID playerId);
    List<SkyjoRoomMemberDataModel> readRoomMembers(UUID roomId);
    SkyjoRoomMemberDataModel updateRoomMember(SkyjoRoomMemberDataModel roomMember);
    boolean deleteRoomMember(UUID roomId, UUID playerId);
    //#endregion
}
