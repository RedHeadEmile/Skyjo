package net.redheademile.skyjo.services.skyjo.repositories;

import net.redheademile.skyjo.services.skyjo.data.models.SkyjoPlayerDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomMemberDataModel;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static net.redheademile.skyjo.utils.Mapping.map;

@Repository
public class SkyjoRepository implements ISkyjoRepository {
    private static final List<SkyjoRoomDataModel> rooms = new ArrayList<>();
    private static final List<SkyjoRoomMemberDataModel> roomMembers = new ArrayList<>();
    private static final List<SkyjoPlayerDataModel> players = new ArrayList<>();

    //#region Room
    @Override
    public SkyjoRoomDataModel createRoom(SkyjoRoomDataModel room) {
        synchronized (rooms) {
            if (rooms.stream().anyMatch(existingRoom -> existingRoom.getId().equals(room.getId())))
                throw new IllegalStateException("Room already exists");

            rooms.add(room.toBuilder().build());

            return room;
        }
    }

    @Override
    public SkyjoRoomDataModel readRoom(UUID roomId) {
        synchronized (rooms) {
            SkyjoRoomDataModel existingRoom = rooms.stream().filter(room -> room.getId().equals(roomId)).findFirst().orElse(null);
            if (existingRoom == null)
                return null;

            return existingRoom.toBuilder().build();
        }
    }

    @Override
    public SkyjoRoomDataModel readRoom(String roomSecretCode) {
        synchronized (rooms) {
            SkyjoRoomDataModel existingRoom = rooms.stream().filter(room -> room.getSecretCode().equals(roomSecretCode)).findFirst().orElse(null);
            if (existingRoom == null)
                return null;

            return existingRoom.toBuilder().build();
        }
    }

    @Override
    public List<SkyjoRoomDataModel> readRooms() {
        synchronized (rooms) {
            return map(rooms, room -> room.toBuilder().build());
        }
    }

    @Override
    public SkyjoRoomDataModel updateRoom(SkyjoRoomDataModel room) {
        synchronized (rooms) {
            SkyjoRoomDataModel existingRoom = rooms.stream().filter(r -> r.getId().equals(room.getId())).findFirst().orElse(null);
            if (existingRoom == null)
                throw new IllegalStateException("Room does not exists");

            existingRoom.setDisplayName(room.getDisplayName());
            existingRoom.setSecretCode(room.getSecretCode());

            existingRoom.setCurrentTurn(room.getCurrentTurn());
            existingRoom.setCurrentTurnPlayerId(room.getCurrentTurnPlayerId());
            existingRoom.setCurrentTurnPlayerEndAt(room.getCurrentTurnPlayerEndAt());

            existingRoom.setGameBeginAt(room.getGameBeginAt());
            existingRoom.setStatus(room.getStatus());

            existingRoom.setPristineCards(new ArrayList<>(room.getPristineCards()));
            existingRoom.setDiscardedCards(new ArrayList<>(room.getDiscardedCards()));

            return existingRoom.toBuilder().build();
        }
    }

    @Override
    public boolean deleteRoom(UUID roomId) {
        synchronized (rooms) {
            return rooms.removeIf(room -> room.getId().equals(roomId));
        }
    }
    //#endregion

    //#region Player
    @Override
    public SkyjoPlayerDataModel createPlayer(SkyjoPlayerDataModel player) {
        synchronized (players) {
            if (players.stream().anyMatch(existingPlayer -> existingPlayer.getId().equals(player.getId())))
                throw new IllegalStateException("Player already exists");

            players.add(player.toBuilder().build());

            return player;
        }
    }

    @Override
    public SkyjoPlayerDataModel readPlayer(UUID playerId) {
        synchronized (players) {
            SkyjoPlayerDataModel existingPlayer = players.stream().filter(player -> player.getId().equals(playerId)).findFirst().orElse(null);
            if (existingPlayer == null)
                return null;

            return existingPlayer.toBuilder().build();
        }
    }

    @Override
    public List<SkyjoPlayerDataModel> readPlayers() {
        synchronized (players) {
            return map(players, player -> player.toBuilder().build());
        }
    }

    @Override
    public SkyjoPlayerDataModel updatePlayer(SkyjoPlayerDataModel player) {
        synchronized (players) {
            SkyjoPlayerDataModel existingPlayer = players.stream().filter(p -> p.getId().equals(player.getId())).findFirst().orElse(null);
            if (existingPlayer == null)
                throw new IllegalStateException("Player does not exists");

            existingPlayer.setDisplayName(player.getDisplayName());

            return existingPlayer.toBuilder().build();
        }
    }

    @Override
    public boolean deletePlayer(UUID playerId) {
        synchronized (players) {
            return players.removeIf(player -> player.getId().equals(playerId));
        }
    }
    //#endregion

    //#region RoomMember
    @Override
    public SkyjoRoomMemberDataModel createRoomMember(SkyjoRoomMemberDataModel roomMember) {
        synchronized (roomMembers) {
            if (roomMembers.stream().anyMatch(member -> member.getRoomId().equals(roomMember.getRoomId()) && member.getPlayerId().equals(roomMember.getPlayerId())))
                throw new IllegalStateException("Room Member already exists");

            roomMembers.add(roomMember.toBuilder().build());

            return roomMember;
        }
    }

    @Override
    public SkyjoRoomMemberDataModel readRoomMember(UUID playerId) {
        synchronized (roomMembers) {
            List<SkyjoRoomMemberDataModel> existingMembers = roomMembers.stream().filter(member -> member.getPlayerId().equals(playerId)).toList();
            if (existingMembers.isEmpty())
                return null;

            if (existingMembers.size() > 1)
                throw new IllegalStateException("Player is in more than one room");

            return existingMembers.get(0).toBuilder().build();
        }
    }

    @Override
    public List<SkyjoRoomMemberDataModel> readRoomMembers(UUID roomId) {
        synchronized (roomMembers) {
            List<SkyjoRoomMemberDataModel> existingMembers = roomMembers.stream().filter(member -> member.getRoomId().equals(roomId)).toList();
            return map(existingMembers, member -> member.toBuilder().build());
        }
    }

    @Override
    public SkyjoRoomMemberDataModel updateRoomMember(SkyjoRoomMemberDataModel roomMember) {
        synchronized (roomMembers) {
            SkyjoRoomMemberDataModel existingMember = roomMembers.stream().filter(m -> m.getRoomId().equals(roomMember.getRoomId()) && m.getPlayerId().equals(roomMember.getPlayerId())).findFirst().orElse(null);
            if (existingMember == null)
                throw new IllegalStateException("This room member doesn't exists");

            existingMember.setRealBoard(new ArrayList<>(roomMember.getRealBoard()));
            existingMember.setShownBoard(Arrays.copyOf(roomMember.getShownBoard(), roomMember.getShownBoard().length));

            existingMember.setScores(new ArrayList<>(roomMember.getScores()));

            return existingMember.toBuilder().build();
        }
    }

    @Override
    public boolean deleteRoomMember(UUID roomId, UUID playerId) {
        synchronized (roomMembers) {
            return roomMembers.removeIf(member -> member.getRoomId().equals(roomId) && member.getPlayerId().equals(playerId));
        }
    }
    //#endregion
}
