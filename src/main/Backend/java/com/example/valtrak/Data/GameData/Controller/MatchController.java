package com.example.valtrak.Data.GameData.Controller;

import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.*;
import com.example.valtrak.Data.GameData.Service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Matches between two signed-in players. Who is acting always comes from the sign-in token, never from the request.
 */
@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public List<MatchSummary> list(Authentication auth) {
        return matchService.list(playerId(auth));
    }

    @PostMapping("/challenge")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchSummary challenge(Authentication auth, @RequestBody ChallengeRequest request) {
        return matchService.challenge(playerId(auth), request);
    }

    @PostMapping("/{matchId}/accept")
    public MatchSummary accept(Authentication auth, @PathVariable Long matchId, @RequestBody AcceptRequest request) {
        return matchService.accept(playerId(auth), matchId, request);
    }

    @PostMapping("/{matchId}/decline")
    public MatchSummary decline(Authentication auth, @PathVariable Long matchId) {
        return matchService.decline(playerId(auth), matchId);
    }

    @PostMapping("/{matchId}/cancel")
    public MatchSummary cancel(Authentication auth, @PathVariable Long matchId) {
        return matchService.cancel(playerId(auth), matchId);
    }

    @GetMapping("/{matchId}")
    public GameView view(Authentication auth, @PathVariable Long matchId) {
        return matchService.view(playerId(auth), matchId);
    }

    @GetMapping("/{matchId}/log")
    public List<LogLine> log(Authentication auth, @PathVariable Long matchId,
                             @RequestParam(defaultValue = "0") int after) {
        return matchService.log(playerId(auth), matchId, after);
    }

    @PostMapping("/{matchId}/actions")
    public ActionResponse act(Authentication auth, @PathVariable Long matchId, @RequestBody ActionRequest request) {
        return matchService.act(playerId(auth), matchId, request);
    }

    @PostMapping("/{matchId}/resign")
    public ActionResponse resign(Authentication auth, @PathVariable Long matchId) {
        return matchService.resign(playerId(auth), matchId);
    }

    private static Long playerId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
