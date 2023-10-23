package net.redheademile.skyjo.services.skyjo.repositories;

import net.redheademile.skyjo.services.skyjo.data.models.SkyjoPlayerDataModel;
import net.redheademile.skyjo.services.skyjo.data.models.SkyjoRoomDataModel;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequestScope
public class SkyjoRepository implements ISkyjoRepository {
    private static final List<SkyjoRoomDataModel> rooms = new ArrayList<>();
    private static final List<SkyjoPlayerDataModel> players = new ArrayList<>();

    @Override
    public SkyjoRoomDataModel createRoom(SkyjoRoomDataModel room) {
        synchronized (rooms) {
            if (rooms.stream().anyMatch(existingRoom -> existingRoom.getId().equals(room.getId())))
                throw new IllegalStateException("Room already exists");

            rooms.add(room);

            return room;
        }
    }

    @Override
    public SkyjoRoomDataModel readRoom(UUID roomId) {
        synchronized (rooms) {
            return rooms.stream().filter(room -> room.getId().equals(roomId)).findFirst().orElse(null);
        }
    }

    @Override
    public SkyjoRoomDataModel readRoom(String roomSecretCode) {
        synchronized (rooms) {
            return rooms.stream().filter(room -> room.getSecretCode().equals(roomSecretCode)).findFirst().orElse(null);
        }
    }

    @Override
    public List<SkyjoRoomDataModel> readRooms() {
        synchronized (rooms) {
            return rooms;
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
            existingRoom.setPlayers(room.getPlayers());

            return existingRoom;
        }
    }

    @Override
    public SkyjoPlayerDataModel createPlayer(SkyjoPlayerDataModel player) {
        synchronized (players) {
            if (players.stream().anyMatch(existingPlayer -> existingPlayer.getId().equals(player.getId())))
                throw new IllegalStateException("Player already exists");

            players.add(player);

            return player;
        }
    }

    @Override
    public SkyjoPlayerDataModel readPlayer(UUID playerId) {
        synchronized (players) {
            return players.stream().filter(player -> player.getId().equals(playerId)).findFirst().orElse(null);
        }
    }

    @Override
    public List<SkyjoPlayerDataModel> readPlayers() {
        synchronized (players) {
            return players;
        }
    }

    @Override
    public SkyjoPlayerDataModel updatePlayer(SkyjoPlayerDataModel player) {
        synchronized (players) {
            SkyjoPlayerDataModel existingPlayer = players.stream().filter(p -> p.getId().equals(player.getId())).findFirst().orElse(null);
            if (existingPlayer == null)
                throw new IllegalStateException("Player does not exists");

            existingPlayer.setDisplayName(player.getDisplayName());

            return existingPlayer;
        }
    }

    @Override
    public SkyjoRoomDataModel readPlayerRoom(UUID playerId) {
        synchronized (rooms) {
            return rooms.stream().filter(r -> r.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId))).findFirst().orElse(null);
        }
    }
}
