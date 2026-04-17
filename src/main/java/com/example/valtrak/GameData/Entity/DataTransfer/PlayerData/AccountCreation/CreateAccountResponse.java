package com.example.valtrak.GameData.Entity.DataTransfer.PlayerData.AccountCreation;

import lombok.Data;
import lombok.NonNull;
import java.time.LocalDateTime;

@Data
public class CreateAccountResponse {
    @NonNull
    private Long id;
    @NonNull
    private String username;
    @NonNull
    private LocalDateTime creationDate;
}
