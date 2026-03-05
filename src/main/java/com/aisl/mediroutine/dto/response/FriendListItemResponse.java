package com.aisl.mediroutine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendListItemResponse {
    private Long friendId;
    private String nickname;
    private String profileImageUrl;
}