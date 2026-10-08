package com.example.valtrak.Data.GameData.Repository;

import com.example.valtrak.Data.GameData.Entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    Optional<AuthSession> findByTokenHash(String tokenHash);

    /** Just the player id for a valid token: one small query per request instead of loading the session and the player. */
    @org.springframework.data.jpa.repository.Query("select s.player.id from AuthSession s where s.tokenHash = :hash and s.expiresAt > :now")
    Optional<Long> findPlayerIdByToken(@org.springframework.data.repository.query.Param("hash") String tokenHash,
                                       @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);

    void deleteByTokenHash(String tokenHash);

    void deleteByExpiresAtBefore(LocalDateTime time);
}
