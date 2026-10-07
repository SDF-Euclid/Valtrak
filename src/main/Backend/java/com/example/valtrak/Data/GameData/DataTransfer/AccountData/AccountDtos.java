package com.example.valtrak.Data.GameData.DataTransfer.AccountData;

import java.util.List;

/** Request/response bodies for the account endpoints. */
public final class AccountDtos {
    private AccountDtos() {}

    public record RegisterRequest(String email, String displayName, String password, String nation) {}

    public record VerifyRequest(String email, String code) {}

    public record EmailRequest(String email) {}

    public record LoginRequest(String email, String password) {}

    public record UpdateProfileRequest(String displayName, String nation) {}

    public record MessageResponse(String message) {}

    public record NationDto(String name, String abbreviation) {}

    public record FavoritesResponse(List<Long> cardIds) {}

    public record ProfileDto(Long id, String email, String displayName, String nation, String nationAbbreviation) {}

    public record LoginResponse(String token, ProfileDto profile) {}
}
