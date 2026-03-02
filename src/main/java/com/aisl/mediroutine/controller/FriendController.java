package com.aisl.mediroutine.controller;

import com.aisl.mediroutine.dto.request.FriendCreateRequest;
import com.aisl.mediroutine.dto.request.FriendUpdateRequest;
import com.aisl.mediroutine.dto.response.FriendListItemResponse;
import com.aisl.mediroutine.dto.response.FriendRequestReceivedItemResponse;
import com.aisl.mediroutine.dto.response.FriendUpdateResponse;
import com.aisl.mediroutine.global.response.ApiResponse;
import com.aisl.mediroutine.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/friends/requests/received")
    public ResponseEntity<ApiResponse<List<FriendRequestReceivedItemResponse>>> receivedRequests(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        var response = friendService.getReceivedRequests(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("받은 친구 요청 조회 성공", response));
    }

    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<FriendListItemResponse>>> getFriends(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<FriendListItemResponse> response = friendService.getMyFriends(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("친구 목록 조회 성공", response));
    }

    @PostMapping("/friends")
    public ResponseEntity<ApiResponse<Void>> requestFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid FriendCreateRequest request
    ) {
        friendService.requestFriend(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("친구 신청이 전송되었습니다."));
    }

    @PatchMapping("/friends/{friendId}")
    public ResponseEntity<ApiResponse<FriendUpdateResponse>> updateFriendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId,
            @RequestBody @Valid FriendUpdateRequest request
    ) {
        FriendUpdateResponse response =
                friendService.updateFriendRequest(userDetails.getUsername(), friendId, request);

        return ResponseEntity.ok(ApiResponse.success("친구 요청 처리 완료", response));
    }

    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId
    ) {
        friendService.deleteFriend(userDetails.getUsername(), friendId);
        return ResponseEntity.noContent().build();
    }

}