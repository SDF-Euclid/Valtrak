package com.example.valtrak.Data.GameData.Controller;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.*;
import com.example.valtrak.Data.GameData.Service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accounts;

    // ── Open endpoints (guests) ──────────────────────────────────────────────

    @GetMapping("/nations")
    public List<NationDto> nations() {
        return accounts.getNations();
    }

    @PostMapping("/account/register")
    public MessageResponse register(@RequestBody RegisterRequest request) {
        return accounts.register(request);
    }

    @PostMapping("/account/verify")
    public LoginResponse verify(@RequestBody VerifyRequest request) {
        return accounts.verify(request);
    }

    @PostMapping("/account/resend-code")
    public MessageResponse resendCode(@RequestBody EmailRequest request) {
        return accounts.resendCode(request);
    }

    @PostMapping("/account/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return accounts.login(request);
    }

    // ── Signed-in endpoints ──────────────────────────────────────────────────

    @PostMapping("/account/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authorization) {
        accounts.logout(authorization.substring("Bearer ".length()).trim());
    }

    @GetMapping("/account/me")
    public ProfileDto me(Authentication auth) {
        return accounts.getProfile(playerId(auth));
    }

    @PutMapping("/account/me")
    public ProfileDto updateMe(Authentication auth, @RequestBody UpdateProfileRequest request) {
        return accounts.updateProfile(playerId(auth), request);
    }

    @GetMapping("/account/me/favorites")
    public FavoritesResponse favorites(Authentication auth) {
        return accounts.getFavorites(playerId(auth));
    }

    @PutMapping("/account/me/favorites/{cardId}")
    public FavoritesResponse addFavorite(Authentication auth, @PathVariable Long cardId) {
        return accounts.addFavorite(playerId(auth), cardId);
    }

    @DeleteMapping("/account/me/favorites/{cardId}")
    public FavoritesResponse removeFavorite(Authentication auth, @PathVariable Long cardId) {
        return accounts.removeFavorite(playerId(auth), cardId);
    }

    private static Long playerId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
