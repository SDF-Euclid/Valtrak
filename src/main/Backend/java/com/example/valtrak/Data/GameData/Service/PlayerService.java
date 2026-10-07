package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.Entity.Player;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.PlayerNotFoundException;
import com.example.valtrak.Data.GameData.Repository.PlayerRepository;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PlayerService {

    /*==================== VARIABLES ====================*/

    private final PlayerRepository playerRepository;

    /*===================================================*/

    /*==================== SEARCH METHOD IMPLEMENTATION ====================*/

    public Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("Player with ID: " + id + "not found"));
    }

    public Player findByUserName(String userName) {
        return playerRepository.findByUserName(userName)
                .orElseThrow(() -> new PlayerNotFoundException("Player with username: " + userName + "not found"));
    }

    public Player findByDisplayName(String displayName) {
        return playerRepository.findByDisplayName(displayName)
                .orElseThrow(() -> new PlayerNotFoundException("Player with display name: " + displayName + "not found"));
    }

    public Player findByEmail(@Email String email) {
        return playerRepository.findByEmail(email)
                .orElseThrow(() -> new PlayerNotFoundException("Player with email: " + email + "not found"));
    }

    /*======================================================================*/
}
