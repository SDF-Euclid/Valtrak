package com.example.valtrak.GameData.Controller;

import com.example.valtrak.GameData.Entity.DataTransfer.PlayerData.AccountCreation.*;
import com.example.valtrak.GameData.Service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/player/")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("create")
    public CreateAccountResponse createPlayer(@RequestBody CreateAccountRequest newPlayer) {return playerService.createNewAccount(newPlayer);}
}
