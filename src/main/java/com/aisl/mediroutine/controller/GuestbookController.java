package com.aisl.mediroutine.controller;

import com.aisl.mediroutine.dto.request.GuestbookCreateRequest;
import com.aisl.mediroutine.dto.response.GuestbookCreateResponse;
import com.aisl.mediroutine.dto.response.GuestbookListItemResponse;
import com.aisl.mediroutine.global.response.ApiResponse;
import com.aisl.mediroutine.service.GuestbookService;
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
public class GuestbookController {

    private final GuestbookService guestbookService;

    /**
     * GET /users/{userId}/guestbook
     */
    @GetMapping("/users/{userId}/guestbook")
    public ResponseEntity<ApiResponse<List<GuestbookListItemResponse>>> getGuestbook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId
    ) {
        List<GuestbookListItemResponse> response =
                guestbookService.getGuestbook(userDetails.getUsername(), userId);

        return ResponseEntity.ok(ApiResponse.success("방명록 조회 성공", response));
    }

    /**
     * POST /users/{userId}/guestbook
     */
    @PostMapping("/users/{userId}/guestbook")
    public ResponseEntity<ApiResponse<GuestbookCreateResponse>> createGuestbook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId,
            @RequestBody @Valid GuestbookCreateRequest request
    ) {
        GuestbookCreateResponse response =
                guestbookService.createGuestbook(userDetails.getUsername(), userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("방명록 작성 완료", response));
    }

    /**
     * DELETE /guestbook/{guestbookId}
     */
    @DeleteMapping("/guestbook/{guestbookId}")
    public ResponseEntity<Void> deleteGuestbook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long guestbookId
    ) {
        guestbookService.deleteGuestbook(userDetails.getUsername(), guestbookId);
        return ResponseEntity.noContent().build();
    }
}