package com.aisl.mediroutine.dto.response;

import com.aisl.mediroutine.entity.Guestbook;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GuestbookListItemResponse {

    private Long guestbookId;
    private Long writerId;
    private String writerNickname;
    private String content;
    private LocalDateTime createdAt;

    public static GuestbookListItemResponse from(Guestbook g) {
        return new GuestbookListItemResponse(
                g.getId(),
                g.getWriter().getId(),
                g.getWriter().getName(), // nickname 컬럼 없으니 name으로 대체
                g.getContent(),
                g.getCreatedAt()
        );
    }
}