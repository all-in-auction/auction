//package com.auction;
//
//import com.auction.domain.auth.service.AuthService;
//import com.auction.domain.point.entity.Point;
//import com.auction.domain.point.repository.PointRepository;
//import com.auction.domain.user.entity.User;
//import com.auction.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@SpringBootTest
//@ActiveProfiles("dev")
//public class DummyDataTest {
//
//    @Autowired
//    private AuthService authService;
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private PointRepository pointRepository;
//
//    @Test
//    @Rollback(value = false)
//    void 더미_유저_천명_생성() {
//        List<User> userList = new ArrayList<>(1000);
//
//        for (int i = 1; i <= 1000; i++) {
//            Long id = (long) i;
//            String email = "email" + i + "@email.com";
//            String pw = "1234";
//            String name = "test";
//            String nickName = "test";
//            int zipCode = 1;
//            String address1 = "incheon";
//            String address2 = "songdo";
//            String authority = "user";
//
//            User user = new User(id, email, pw, name, nickName, zipCode, address1, address2, authority);
//            userList.add(user);
//
//            if (userList.size() == 1000) {
//                userRepository.saveAll(userList);
//                userList.clear();
//            }
//        }
//
//        if (!userList.isEmpty()) {
//            userRepository.saveAll(userList);
//            userList.clear();
//        }
//
//        // then
//        long count = userRepository.count();
//        assertEquals(count, 10_000);
//    }
//
//    @Test
//    @Rollback(value = false)
//    void 포인트_천만원_지급() {
//
//        int pointAmount = 10000000;
//        for (int i = 1; i <= 1000; i++) {
//            Long id = (long) i;
//            String email = "email" + i + "@email.com";
//            User user = userRepository.findByEmail(email).orElseThrow();
//
//            Point point = new Point(pointAmount, user);
//            pointRepository.save(point);
//        }
//
//        // then
//        long count = pointRepository.count();
//        assertEquals(count, 1000);
//    }
//}
//
