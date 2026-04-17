package com.example.valtrak.Gameplay.Controller;

import com.example.valtrak.Data.GameData.DataTransfer.PlayerData.AccountCreation.CreateAccountRequest;
import com.example.valtrak.Data.GameData.DataTransfer.PlayerData.AccountCreation.CreateAccountResponse;
import com.example.valtrak.Data.GameData.Service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
@RequestMapping("/player/")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("create")
    public CreateAccountResponse createPlayer(@RequestBody CreateAccountRequest newPlayer) {return playerService.createNewAccount(newPlayer);}

}
