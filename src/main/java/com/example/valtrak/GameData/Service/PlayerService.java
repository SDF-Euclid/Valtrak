package com.example.valtrak.GameData.Service;

import com.example.valtrak.GameData.Entity.DataTransfer.PlayerData.AccountCreation.CreateAccountRequest;
import com.example.valtrak.GameData.Entity.DataTransfer.PlayerData.AccountCreation.CreateAccountResponse;
import com.example.valtrak.GameData.Entity.Player;
import com.example.valtrak.GameData.ExceptionHandling.Exceptions.PlayerNotFoundException;
import com.example.valtrak.GameData.Repository.Players.PlayerRepository;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PlayerService {

    /*==================== VARIABLES ====================*/

    private final PlayerRepository playerRepository;

    /**
     * An auto-injected Argon2 PasswordEncoder for handling password hashing
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    /*==================== LOGIC METHOD IMPLEMENTATION ====================*/

    /**
     *
     * @param createAccountRequest
     * @return
     */
    public CreateAccountResponse createNewAccount(@Nonnull CreateAccountRequest createAccountRequest) {
        Player newPlayer = new Player(createAccountRequest.getUserName(),
                                      createAccountRequest.getDisplayName(),
                                      createAccountRequest.getDisplayNation(),
                                      createAccountRequest.getEmail()
        );
        newPlayer.setPassword(passwordEncoder.encode(createAccountRequest.getPassword()));
        playerRepository.save(newPlayer);
        return new CreateAccountResponse(newPlayer.getId(), newPlayer.getUserName(), newPlayer.getCreationDate());
    }

    /*=====================================================================*/
}
