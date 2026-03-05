package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.Guestbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

    // 5. 작성일 기준 최신순 정렬
    List<Guestbook> findAllByOwner_IdOrderByCreatedAtDesc(Long ownerId);
}