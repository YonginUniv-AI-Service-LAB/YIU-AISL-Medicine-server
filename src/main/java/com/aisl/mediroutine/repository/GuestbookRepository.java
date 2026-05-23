package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.Guestbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

    // 5. 작성일 기준 최신순 정렬
    @Query("SELECT g FROM Guestbook g JOIN FETCH g.writer WHERE g.owner.id = :ownerId ORDER BY g.createdAt DESC")
    List<Guestbook> findAllByOwnerIdWithWriter(@Param("ownerId") Long ownerId);

    // 회원탈퇴 시 해당 유저가 owner이거나 writer인 방명록 전체 삭제 (User FK 제약 해제용)
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Guestbook g WHERE g.owner.id = :userId OR g.writer.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}