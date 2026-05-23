package com.aisl.mediroutine.service;

import com.aisl.mediroutine.dto.request.FriendCreateRequest;
import com.aisl.mediroutine.dto.request.FriendUpdateRequest;
import com.aisl.mediroutine.dto.response.FriendListItemResponse;
import com.aisl.mediroutine.dto.response.FriendRequestReceivedItemResponse;
import com.aisl.mediroutine.dto.response.FriendUpdateResponse;
import com.aisl.mediroutine.entity.Friend;
import com.aisl.mediroutine.entity.User;
import com.aisl.mediroutine.global.exception.CustomException;
import com.aisl.mediroutine.repository.FriendRepository;
import com.aisl.mediroutine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FriendListItemResponse> getMyFriends(String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<Friend> relations = friendRepository.findAllByUserIdAndStatus(me.getId(), Friend.Status.ACCEPTED);

        return relations.stream().map(f -> {
            User other = f.getRequester().getId().equals(me.getId()) ? f.getReceiver() : f.getRequester();
            return new FriendListItemResponse(
                    f.getId(),
                    other.getId(),
                    other.getName(), // nickname 컬럼 없으니 name으로 대체
                    null             // profileImageUrl 컬럼 없으니 null
            );
        }).toList();
    }

    // 캘린더 권한검사용(1번 API에서 사용)
    @Transactional(readOnly = true)
    public boolean isFriendAccepted(Long me, Long other) {
        return friendRepository.countAcceptedBetween(me, other, Friend.Status.ACCEPTED) > 0;
    }

    @Transactional
    public void requestFriend(String myEmail, FriendCreateRequest request) {

        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        User target = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "대상 사용자를 찾을 수 없습니다."));

        if (me.getId().equals(target.getId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "자신에게 친구 신청할 수 없습니다.");
        }

        friendRepository.findRelationBetween(me.getId(), target.getId()).ifPresent(existing -> {
            if (existing.getStatus() == Friend.Status.ACCEPTED) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "이미 친구입니다.");
            }
            if (existing.getStatus() == Friend.Status.PENDING) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "이미 친구 신청 상태입니다.");
            }
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 친구 신청 상태입니다.");
        });

        Friend friend = Friend.builder()
                .requester(me)
                .receiver(target)
                .status(Friend.Status.PENDING)
                .build();

        try {
            friendRepository.save(friend);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 친구 신청 상태입니다.");
        }
    }

    @Transactional
    public FriendUpdateResponse updateFriendRequest(String email, Long friendRelationId, FriendUpdateRequest request) {

        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Friend friend = friendRepository.findById(friendRelationId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "친구 요청을 찾을 수 없습니다."));

        if (friend.getStatus() != Friend.Status.PENDING) {
            throw new CustomException(HttpStatus.NOT_FOUND, "친구 요청을 찾을 수 없습니다.");
        }

        // 신청받은 사용자만 처리 가능
        if (!friend.getReceiver().getId().equals(me.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "처리 권한이 없습니다.");
        }

        Friend.Status newStatus;
        try {
            newStatus = Friend.Status.valueOf(request.getStatus());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "올바른 상태 값을 입력하세요.");
        }

        if (newStatus != Friend.Status.ACCEPTED && newStatus != Friend.Status.REJECTED) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "올바른 상태 값을 입력하세요.");
        }

        friend.updateStatus(newStatus);

        // 응답의 friendId는 "상대 유저 id" (요청자)
        return FriendUpdateResponse.from(friend.getRequester().getId(), newStatus);
    }
    @Transactional
    public void deleteFriend(String email, Long friendRelationId) {

        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Friend friend = friendRepository.findById(friendRelationId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "친구 관계를 찾을 수 없습니다."));

        if (friend.getStatus() != Friend.Status.ACCEPTED) {
            throw new CustomException(HttpStatus.NOT_FOUND, "친구 관계를 찾을 수 없습니다.");
        }

        boolean isParty = friend.getRequester().getId().equals(me.getId())
                || friend.getReceiver().getId().equals(me.getId());

        if (!isParty) {
            // 명세에는 없지만, 안전하게 권한 체크
            throw new CustomException(HttpStatus.FORBIDDEN, "처리 권한이 없습니다.");
        }

        friendRepository.delete(friend);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestReceivedItemResponse> getReceivedRequests(String email) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return friendRepository.findAllByReceiverIdAndStatus(me.getId(), Friend.Status.PENDING)
                .stream()
                .map(f -> new FriendRequestReceivedItemResponse(
                        f.getId(),                    // relationId
                        f.getRequester().getId(),     // fromUserId
                        f.getRequester().getName()    // fromNickname (name으로 대체)
                ))
                .toList();
    }
}