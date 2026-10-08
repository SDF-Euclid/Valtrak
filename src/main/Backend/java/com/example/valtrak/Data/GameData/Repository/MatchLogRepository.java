package com.example.valtrak.Data.GameData.Repository;

import com.example.valtrak.Data.GameData.Entity.MatchLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchLogRepository extends JpaRepository<MatchLogEntry, Long> {

    List<MatchLogEntry> findByMatchIdAndSeqGreaterThanOrderBySeq(Long matchId, int seq);

    @Query("select coalesce(max(e.seq), 0) from MatchLogEntry e where e.matchId = :matchId")
    int lastSeq(@Param("matchId") Long matchId);
}
