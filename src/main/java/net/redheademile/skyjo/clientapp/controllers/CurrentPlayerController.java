package net.redheademile.skyjo.clientapp.controllers;

import jakarta.validation.Valid;
import net.redheademile.skyjo.clientapp.models.*;
import net.redheademile.skyjo.services.skyjo.ISkyjoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

@RestController
@RequestScope
@RequestMapping("/current-player")
public class CurrentPlayerController {
    private final ISkyjoService skyjoService;

    @Autowired
    public CurrentPlayerController(ISkyjoService skyjoService) {
        this.skyjoService = skyjoService;
    }

    @GetMapping
    public ResponseEntity<SkyjoPlayerViewModel> showCurrentPlayer() {
        return ResponseEntity.ok(SkyjoPlayerViewModel.fromBusinessModel(skyjoService.getCurrentPlayer()));
    }

    @PutMapping
    public ResponseEntity<SkyjoPlayerViewModel> updateCurrentPlayerSettings(@RequestBody @Valid SkyjoCurrentPlayerSettingsUpdateRequest request) {
        return ResponseEntity.ok(SkyjoPlayerViewModel.fromBusinessModel(skyjoService.setCurrentPlayerName(request.getDisplayName())));
    }

    @PutMapping("room")
    public ResponseEntity<SkyjoRoomViewModel> updateCurrentPlayerRoom(@RequestBody @Valid SkyjoCurrentPlayerRoomUpdateRequestViewModel request) {
        return ResponseEntity.ok(SkyjoRoomViewModel.fromBusinessModel(skyjoService.addCurrentPlayerToRoom(request.getRoomSecretCode())));
    }

    @DeleteMapping("room")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCurrentPlayerRoom() {
        skyjoService.removeCurrentPlayerFromRoom();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("room/display-name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateCurrentPlayerRoomDisplayName(@RequestBody @Valid SkyjoCurrentPlayerRoomDisplayNameRequestViewModel request) {
        skyjoService.setCurrentPlayerRoomDisplayName(request.getNewDisplayName());
        return ResponseEntity.noContent().build();
    }
}
