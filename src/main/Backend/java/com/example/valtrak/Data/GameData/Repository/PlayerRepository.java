package com.example.valtrak.Data.GameData.Repository;

import com.example.valtrak.Data.GameData.Entity.Player;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Override
    @Nonnull Optional<Player> findById(@Nonnull Long id);

    Optional<Player> findByUserName(String userName);

    Optional<Player> findByDisplayName(String displayName);

    Optional<Player> findByEmail(@Email String email);

    /** Loads a player and blocks other writers until the transaction ends, so attempt counters can't be raced. */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Player p where p.email = :email")
    Optional<Player> lockByEmail(@org.springframework.data.repository.query.Param("email") String email);

    boolean existsByDisplayNameIgnoreCase(String displayName);

    Optional<Player> findByDisplayNameIgnoreCase(String displayName);
}
