package com.aisl.mediroutine.dto.response;

import com.aisl.mediroutine.entity.Friend;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendUpdateResponse {
    private Long friendId;
    private String status;

    public static FriendUpdateResponse from(Long otherUserId, Friend.Status status) {
        return new FriendUpdateResponse(otherUserId, status.name());
    }
}