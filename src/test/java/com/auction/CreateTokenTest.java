package com.auction;

import com.auction.common.utils.JwtUtil;
import com.auction.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.io.IOException;

// TODO 사용할 때 주석 해제 (테스트용 JWT Token 생성)
@SpringBootTest
public class CreateTokenTest {
    @Autowired
    private JwtUtil jwtUtil;

//    @Test
//    @DisplayName("JWT Token 생성")
//    public void createUserToken() {
//        try {
//            // 파일 작성용 FileWriter 생성
//            FileWriter writer = new FileWriter("jwtTokenList.txt");
//
//            // 1부터 10,000까지 반복
//            for (long i = 1; i <= 1000; i++) {
//                String data = jwtUtil.createToken(i, "email" + i + "@email.com", UserRole.USER);
//
//                // 파일에 작성
//                writer.write(data + "\r\n");
//            }
//
//            // 파일 닫기
//            writer.close();
//            System.out.println("파일이 성공적으로 생성되었습니다.");
//        } catch (IOException e) {
//            System.out.println("파일 작성 중 오류가 발생했습니다.");
//            e.printStackTrace();
//        }
//    }
}

