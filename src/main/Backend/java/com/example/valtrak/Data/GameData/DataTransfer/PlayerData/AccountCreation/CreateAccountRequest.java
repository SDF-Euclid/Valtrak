package com.example.valtrak.Data.GameData.DataTransfer.PlayerData.AccountCreation;

import com.example.valtrak.Data.GameData.Config.Annotation.ValidNation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CreateAccountRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String displayName;
    @NotBlank
    @ValidNation
    private String displayNation;
    @NotBlank
    private String password;
    @NotBlank
    @Email private String email;
}
