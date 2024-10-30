package com.auction.domain.auction.repository;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ItemRepositoryTest {

    @Test
    void data_test() {
        // 한국어 설정
        Faker faker = new Faker(new Locale("ko-KR"));

        // 한국어 데이터 생성
        String name = faker.name().fullName(); // 예: "김철수"
        String address = faker.address().fullAddress(); // 예: "서울특별시 강남구 역삼동 123-45"
        String productName = faker.commerce().productName(); // 예: "한국 전통 가구"
        String description = faker.lorem().paragraph(); // 예: "이 제품은 최상의 품질을 자랑합니다."

        // 생성된 데이터 출력
        System.out.println("이름: " + name);
        System.out.println("주소: " + address);
        System.out.println("제품명: " + productName);
        System.out.println("설명: " + description);

        System.out.println(faker.commerce().department());
    }

}