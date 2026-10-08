package com.example.valtrak.Data.GameData.Repository;

import com.example.valtrak.Data.GameData.Entity.MatchRecord;
import com.example.valtrak.Data.GameData.Enums.MatchStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<MatchRecord, Long> {

    /** Loads a match and blocks other writers until this transaction ends, so two moves can't collide. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MatchRecord m where m.id = :id")
    Optional<MatchRecord> lockById(@Param("id") Long id);

    List<MatchRecord> findByPlayer0IdOrPlayer1IdOrderByUpdatedAtDesc(Long player0Id, Long player1Id);

    long countByPlayer0IdAndStatus(Long playerId, MatchStatus status);

    @Query("select count(m) from MatchRecord m where m.status = :status and (m.player0.id = :playerId or m.player1.id = :playerId)")
    long countForPlayer(@Param("playerId") Long playerId, @Param("status") MatchStatus status);

    List<MatchRecord> findByStatus(MatchStatus status);

    @Query("select count(m) from MatchRecord m join m.challengerDeck c where m.status = com.example.valtrak.Data.GameData.Enums.MatchStatus.PENDING and c = :cardId")
    long countPendingChallengesUsingCard(@Param("cardId") Long cardId);

    /** A player's matches in the given states, newest first (the lobby leaves out declined and cancelled ones). */
    @Query("select m from MatchRecord m where (m.player0.id = :playerId or m.player1.id = :playerId) and m.status in :statuses order by m.updatedAt desc")
    List<MatchRecord> findForPlayer(@Param("playerId") Long playerId, @Param("statuses") java.util.Collection<MatchStatus> statuses,
                                    org.springframework.data.domain.Pageable page);

    boolean existsByPlayer0IdAndPlayer1IdAndStatus(Long player0Id, Long player1Id, MatchStatus status);
}
