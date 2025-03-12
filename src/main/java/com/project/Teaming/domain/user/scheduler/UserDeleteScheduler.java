package com.project.Teaming.domain.user.scheduler;

import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserDeleteScheduler {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;

    @Scheduled(cron = "0 0 3 * * ?")  // 매일 새벽 3시에 확인
    @Transactional
    public void deleteWithdrawUsers() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
        List<User> usersToDelete = userRepository.findDeletableUsers(thresholdDate);

        if (!usersToDelete.isEmpty()) {
            log.info("{}명의 유저가 30일이 경과되어 탈퇴처리 되었습니다.", usersToDelete.size());
            for (User user : usersToDelete) {
                if (user.getPortfolio() != null) {
                    user.userDelete();
                }
                notificationRepository.deleteByUserId(user.getId());
                reportRepository.deleteByReportedUserId(user.getId()); // 신고당한 경우
                reviewRepository.deleteByRevieweeId(user.getId()); // 리뷰를 받은 경우
            }

            userRepository.deleteAll(usersToDelete);
        }
    }
}
