package com.aisl.mediroutine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendRequestReceivedItemResponse {
    private Long relationId;     // friend 테이블 PK(id)
    private Long fromUserId;     // requester_id
    private String fromNickname; // requester name (nickname 대체)
}