package net.redheademile.skyjo.clientapp.controllers;

import jakarta.validation.Valid;
import net.redheademile.skyjo.clientapp.models.SkyjoRoomStoreRequestViewModel;
import net.redheademile.skyjo.clientapp.models.SkyjoRoomViewModel;
import net.redheademile.skyjo.services.skyjo.ISkyjoService;
import net.redheademile.skyjo.services.skyjo.business.models.SkyjoRoomBusinessModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.UUID;

import static net.redheademile.skyjo.utils.Mapping.map;

@RestController
@RequestScope
@RequestMapping("/rooms")
public class RoomController {
    private final ISkyjoService skyjoService;

    @Autowired
    public RoomController(ISkyjoService skyjoService) {
        this.skyjoService = skyjoService;
    }

    @GetMapping
    public ResponseEntity<List<SkyjoRoomViewModel>> indexRoom() {
        return ResponseEntity.ok(map(skyjoService.getRooms(), SkyjoRoomViewModel::fromBusinessModel));
    }

    @GetMapping("{id}")
    public ResponseEntity<SkyjoRoomViewModel> showRoom(@PathVariable String id) {
        SkyjoRoomBusinessModel room = null;
        try {
            UUID uuid = UUID.fromString(id);
            room = skyjoService.getRoom(uuid);
        }
        catch (IllegalArgumentException e) {
            room = skyjoService.getRoom(id);
        }

        if (room == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(SkyjoRoomViewModel.fromBusinessModel(room));
    }

    @PostMapping
    public ResponseEntity<SkyjoRoomViewModel> storeRoom(@RequestBody @Valid SkyjoRoomStoreRequestViewModel request) {
        SkyjoRoomBusinessModel newRoom = skyjoService.addRoom(request.getDisplayName());
        return ResponseEntity.ok(SkyjoRoomViewModel.fromBusinessModel(newRoom));
    }
}
