package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("""
        SELECT COUNT(f)
        FROM Friend f
        WHERE f.status = :status
          AND (
            (f.requester.id = :a AND f.receiver.id = :b)
            OR (f.requester.id = :b AND f.receiver.id = :a)
          )
    """)
    long countAcceptedBetween(@Param("a") Long a, @Param("b") Long b, @Param("status") Friend.Status status);

    @Query(value = """
        SELECT *
        FROM friend
        WHERE LEAST(requester_id, receiver_id) = LEAST(:a, :b)
          AND GREATEST(requester_id, receiver_id) = GREATEST(:a, :b)
        LIMIT 1
        """, nativeQuery = true)
    Optional<Friend> findRelationBetween(@Param("a") Long a, @Param("b") Long b);

    List<Friend> findAllByReceiverIdAndStatus(Long receiverId, Friend.Status status);

    // 회원탈퇴 시 해당 유저가 포함된 친구 관계 전체 삭제 (User FK 제약 해제용)
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Friend f WHERE f.requester.id = :userId OR f.receiver.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}