package com.example.valtrak.Data.GameData.Controller;

import com.example.valtrak.Data.GameData.DataTransfer.DamageData.CombatResult;
import com.example.valtrak.Data.GameData.DataTransfer.GameData.*;
import com.example.valtrak.Data.GameData.Entity.GameState.FieldUnit;
import com.example.valtrak.Data.GameData.Entity.GameState.Game;
import com.example.valtrak.Data.GameData.Service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity<Game> createGame(@RequestBody CreateGameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createGame(request));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

    @PostMapping("/{gameId}/advance-phase")
    public ResponseEntity<Game> advancePhase(@PathVariable Long gameId,
                                             @RequestParam Long playerId) {
        return ResponseEntity.ok(gameService.advancePhase(gameId, playerId));
    }

    @PostMapping("/{gameId}/play-resource")
    public ResponseEntity<Game> playResource(@PathVariable Long gameId,
                                             @RequestParam Long playerId,
                                             @RequestBody PlayResourceRequest request) {
        return ResponseEntity.ok(gameService.playResource(gameId, playerId, request));
    }

    @PostMapping("/{gameId}/deploy")
    public ResponseEntity<FieldUnit> deploy(@PathVariable Long gameId,
                                            @RequestParam Long playerId,
                                            @RequestBody DeployRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.deploy(gameId, playerId, request));
    }

    @PostMapping("/{gameId}/attack")
    public ResponseEntity<CombatResult> attack(@PathVariable Long gameId,
                                               @RequestParam Long playerId,
                                               @RequestBody AttackRequest request) {
        return ResponseEntity.ok(gameService.attack(gameId, playerId, request));
    }
}
