package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    @Query("""
        SELECT f
        FROM Friend f
        WHERE f.status = :status
          AND (f.requester.id = :userId OR f.receiver.id = :userId)
    """)
    List<Friend> findAllByUserIdAndStatus(@Param("userId") Long userId,
                                          @Param("status") Friend.Status status);

    @Query(value = """
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM friend
            WHERE status = 'ACCEPTED'
              AND LEAST(requester_id, receiver_id) = LEAST(:a, :b)
              AND GREATEST(requester_id, receiver_id) = GREATEST(:a, :b)
        ) THEN 1 ELSE 0
    END
""", nativeQuery = true)
    Long existsAcceptedBetween(@Param("a") Long a, @Param("b") Long b);

    @Query(value = """
        SELECT *
        FROM friend
        WHERE LEAST(requester_id, receiver_id) = LEAST(:a, :b)
          AND GREATEST(requester_id, receiver_id) = GREATEST(:a, :b)
        LIMIT 1
        """, nativeQuery = true)
    Optional<Friend> findRelationBetween(@Param("a") Long a, @Param("b") Long b);

    List<Friend> findAllByReceiverIdAndStatus(Long receiverId, Friend.Status status);
}