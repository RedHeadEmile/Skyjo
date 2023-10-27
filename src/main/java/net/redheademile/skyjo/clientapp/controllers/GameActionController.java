package net.redheademile.skyjo.clientapp.controllers;

import jakarta.validation.Valid;
import net.redheademile.skyjo.clientapp.models.SkyjoGameActionViewModel;
import net.redheademile.skyjo.services.skyjo.ISkyjoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game-action")
public class GameActionController {
    private final ISkyjoService skyjoService;

    @Autowired
    public GameActionController(ISkyjoService skyjoService) {
        this.skyjoService = skyjoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> storeGameAction(@RequestBody @Valid SkyjoGameActionViewModel action) {
        skyjoService.addCurrentPlayerGameAction(action.toBusinessModel());
        return ResponseEntity.noContent().build();
    }
}
