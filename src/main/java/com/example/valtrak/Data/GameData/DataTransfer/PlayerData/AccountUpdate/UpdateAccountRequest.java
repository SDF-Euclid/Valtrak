package com.example.valtrak.Data.GameData.DataTransfer.PlayerData.AccountUpdate;

import lombok.Data;

@Data
public class UpdateAccountRequest {
    private String userName;
    private String displayName;
    private String displayNation;
    private String email;
    private String password;
}
