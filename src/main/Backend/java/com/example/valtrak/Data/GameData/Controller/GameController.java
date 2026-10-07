package com.example.valtrak.Data.GameData.Controller;

import com.example.valtrak.Data.GameData.DataTransfer.DamageData.CombatResult;
import com.example.valtrak.Data.GameData.DataTransfer.GameData.*;
import com.example.valtrak.Data.GameData.Entity.GameState.FieldUnit;
import com.example.valtrak.Data.GameData.Service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * All game endpoints require a signed-in player. Who is acting always comes from
 * the sign-in token, never from the request, so nobody can act as another player.
 */
@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity<GameView> createGame(Authentication auth, @RequestBody CreateGameRequest request) {
        Long me = playerId(auth);
        var game = gameService.createGame(request, me);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.getGameView(game.getId(), me));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameView> getGame(Authentication auth, @PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getGameView(gameId, playerId(auth)));
    }

    @PostMapping("/{gameId}/advance-phase")
    public ResponseEntity<GameView> advancePhase(Authentication auth, @PathVariable Long gameId) {
        Long me = playerId(auth);
        gameService.advancePhase(gameId, me);
        return ResponseEntity.ok(gameService.getGameView(gameId, me));
    }

    @PostMapping("/{gameId}/play-resource")
    public ResponseEntity<GameView> playResource(Authentication auth, @PathVariable Long gameId,
                                                 @RequestBody PlayResourceRequest request) {
        Long me = playerId(auth);
        gameService.playResource(gameId, me, request);
        return ResponseEntity.ok(gameService.getGameView(gameId, me));
    }

    @PostMapping("/{gameId}/deploy")
    public ResponseEntity<FieldUnit> deploy(Authentication auth, @PathVariable Long gameId,
                                            @RequestBody DeployRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.deploy(gameId, playerId(auth), request));
    }

    @PostMapping("/{gameId}/attack")
    public ResponseEntity<CombatResult> attack(Authentication auth, @PathVariable Long gameId,
                                               @RequestBody AttackRequest request) {
        return ResponseEntity.ok(gameService.attack(gameId, playerId(auth), request));
    }

    private static Long playerId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
