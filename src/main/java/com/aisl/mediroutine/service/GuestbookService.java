package com.aisl.mediroutine.service;

import com.aisl.mediroutine.dto.request.GuestbookCreateRequest;
import com.aisl.mediroutine.dto.response.GuestbookCreateResponse;
import com.aisl.mediroutine.dto.response.GuestbookListItemResponse;
import com.aisl.mediroutine.entity.Guestbook;
import com.aisl.mediroutine.entity.User;
import com.aisl.mediroutine.global.exception.CustomException;
import com.aisl.mediroutine.repository.GuestbookRepository;
import com.aisl.mediroutine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestbookService {

    private final UserRepository userRepository;
    private final GuestbookRepository guestbookRepository;
    private final FriendService friendService;

    /**
     * GET /users/{userId}/guestbook
     * - 본인 또는 친구만 조회 가능
     */
    @Transactional(readOnly = true)
    public List<GuestbookListItemResponse> getGuestbook(String loginEmail, Long ownerId) {

        // 1) 로그인 사용자 확인
        User me = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 2) userId 존재 여부 확인
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 3) 권한 검사
        if (!me.getId().equals(owner.getId())) {
            boolean isFriend = friendService.isFriendAccepted(me.getId(), owner.getId());
            if (!isFriend) {
                throw new CustomException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다.");
            }
        }

        // 4) 목록 조회 + 5) 최신순 정렬
        return guestbookRepository.findAllByOwner_IdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(GuestbookListItemResponse::from)
                .toList();
    }

    /**
     * POST /users/{userId}/guestbook
     * - 본인 방명록 작성 불가
     * - 친구에게만 작성 가능
     */
    @Transactional
    public GuestbookCreateResponse createGuestbook(String loginEmail, Long ownerId, GuestbookCreateRequest request) {

        // 1) 로그인 사용자 확인
        User me = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 2) userId 존재 여부 확인
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 3) 권한 검사
        if (me.getId().equals(owner.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "자신의 방명록에는 작성할 수 없습니다.");
        }

        boolean isFriend = friendService.isFriendAccepted(me.getId(), owner.getId());
        if (!isFriend) {
            throw new CustomException(HttpStatus.FORBIDDEN, "작성 권한이 없습니다.");
        }

        // 4) content 검증은 DTO Validation으로 처리 (@NotBlank/@Size)

        // 5) 저장
        Guestbook guestbook = Guestbook.builder()
                .owner(owner)
                .writer(me)
                .content(request.getContent())
                .build();

        Guestbook saved = guestbookRepository.save(guestbook);
        return GuestbookCreateResponse.from(saved);
    }

    /**
     * DELETE /guestbook/{guestbookId}
     * - 작성자 or 방명록 주인만 삭제 가능
     */
    @Transactional
    public void deleteGuestbook(String loginEmail, Long guestbookId) {

        // 1) 로그인 사용자 확인
        User me = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 2) guestbook 조회
        Guestbook guestbook = guestbookRepository.findById(guestbookId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "방명록을 찾을 수 없습니다."));

        // 3) 삭제 권한 검사
        boolean isWriter = guestbook.getWriter().getId().equals(me.getId());
        boolean isOwner = guestbook.getOwner().getId().equals(me.getId());

        if (!isWriter && !isOwner) {
            throw new CustomException(HttpStatus.FORBIDDEN, "타인의 방명록을 삭제할 수 없습니다.");
        }

        // 4) 삭제 처리
        guestbookRepository.delete(guestbook);
    }
}