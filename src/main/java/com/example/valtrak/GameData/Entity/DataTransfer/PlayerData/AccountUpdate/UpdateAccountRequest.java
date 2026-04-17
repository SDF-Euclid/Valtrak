package com.example.valtrak.GameData.Entity.DataTransfer.PlayerData.AccountUpdate;

import lombok.Data;

@Data
public class UpdateAccountRequest {
    private String userName;
    private String displayName;
    private String displayNation;
    private String email;
    private String password;
}
