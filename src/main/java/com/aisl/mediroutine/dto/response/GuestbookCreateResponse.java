package com.aisl.mediroutine.dto.response;

import com.aisl.mediroutine.entity.Guestbook;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GuestbookCreateResponse {

    private Long guestbookId;
    private Long ownerId;
    private Long writerId;
    private String content;
    private LocalDateTime createdAt;

    public static GuestbookCreateResponse from(Guestbook g) {
        return new GuestbookCreateResponse(
                g.getId(),
                g.getOwner().getId(),
                g.getWriter().getId(),
                g.getContent(),
                g.getCreatedAt()
        );
    }
}