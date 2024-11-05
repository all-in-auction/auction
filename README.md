# 📦 중고 물품 실시간 경매 서비스

## 프로젝트 개요

중고 물품 실시간 경매 서비스는 사용자들이 중고 물품을 등록하고 실시간으로 경매에 참여할 수 있는 온라인 경매 플랫폼입니다. 사용자는 원하는 물품에 입찰하여 최종 낙찰자가 될 수 있습니다. 이 서비스를 통해 사용자들은 중고 물품을 효율적으로 거래하고, 경쟁적인 입찰을 통해 원하는 물품을 합리적인 가격에 구매할 수 있습니다.

<br>
---
### 아키텍처
<img src="./image/architecture.png"  width = "100%">

### ERD
<img src="./image/erd.png" width = "100%">

<br>

## 2. 주요 기술 스택

### **백엔드**

<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> <img src="https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=jpa&logoColor=white"> <img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white"> <img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white">

### **데이터베이스**

<img src="https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> 

### **CI/CD**

<img src="https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/githubactions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">

### **클라우드 및 모니터링**

<img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"> <img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white"> <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white">

### **로그 관리**

<img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white"> <img src="https://img.shields.io/badge/Logstash-005571?style=for-the-badge&logo=logstash&logoColor=white"> <img src="https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=kibana&logoColor=white">

<br>

## 3. 팀 소개

<table>
  <tbody>
    <tr>
      <td align="center"><a href="https://github.com/devmoonjs"><img src="https://avatars.githubusercontent.com/u/130039001?v=4" width="100px;" alt=""/><br /><sub><b>팀장 : 문정석 </b></sub></a><br /></td>
      <td align="center"><a href="https://github.com/kim-na-ram"><img src="https://avatars.githubusercontent.com/u/32188154?v=4" width="100px;" alt=""/><br /><sub><b>부팀장 : 김나람 </b></sub></a><br /></td>
      <td align="center"><a href="https://github.com/areum0116"><img src="https://avatars.githubusercontent.com/u/61903747?v=4" width="100px;" alt=""/><br /><sub><b>팀원 : 김아름 </b></sub></a><br /></td>
      <td align="center"><a href="https://github.com/inseooo0"><img src="https://avatars.githubusercontent.com/u/67430408?v=4" width="100px;" alt=""/><br /><sub><b>팀원 : 황인서 </b></sub></a><br /></td>
    </tr>
  </tbody>
</table>

<br>

## 4. 최적화 전략 및 상세 기능

---
## **입찰 기능 성능 향상 (CQRS 도입)**

#### 📊 CQRS 도입 전후 성능 비교

CQRS 패턴을 도입하여 경매 시스템의 **입찰 기능 성능**을 향상시켰습니다. 아래는 도입 전후의 성능 테스트 결과 비교입니다.

#### 1. 도입 전 성능 테스트 결과

| 구분      | 표본 수 | 평균 (ms) | 최소값 (ms) | 최대값 (ms) | 표준편차 | 오류 % | 초당 요청 수 | 수신 KB/초 | 전송 KB/초 | 평균 바이트 수 |
|-----------|---------|-----------|-------------|-------------|----------|--------|--------------|------------|------------|---------------|
| HTTP 요청 | 1000    | 8561      | 2506        | 13292       | 3014.49  | 0.20%  | 61.4/sec     | 37.14      | 25.83      | 619.5         |
| **총계**  | **1000**| **8561**  | **2506**    | **13292**   | **3014.49**| **0.20%** | **61.4/sec** | **37.14**  | **25.83**  | **619.5**     |

---

#### 2. 도입 후 성능 테스트 결과

| 구분      | 표본 수 | 평균 (ms) | 최소값 (ms) | 최대값 (ms) | 표준편차 | 오류 % | 초당 요청 수 | 수신 KB/초 | 전송 KB/초 | 평균 바이트 수 |
|-----------|---------|-----------|-------------|-------------|----------|--------|--------------|------------|------------|---------------|
| HTTP 요청 | 1000    | 4599      | 23          | 8235        | 2076.24  | 0.10%  | 89.0/sec     | 50.92      | 37.44      | 585.6         |
| **총계**  | **1000**| **4599**  | **23**      | **8235**    | **2076.24**| **0.10%** | **89.0/sec** | **50.92**  | **37.44**  | **585.6**     |

---

### 💡 성능 개선 요약

- **평균 응답 시간**: 8561ms → 4599ms로 약 46% 감소
- **오류율**: 0.20% → 0.10%로 절반 감소
- **처리량 (Throughput)**:

61.4 요청/초 → 89.0 요청/초로 약 45% 증가
- **데이터 전송 속도**: 수신 KB/초와 전송 KB/초가 각각 증가하여 데이터 처리 효율성 향상

CQRS 패턴을 도입하여 **읽기 작업**과 **쓰기 작업**을 분리함으로써, 경매 서비스의 응답 속도와 처리 성능이 크게 향상되었습니다. 이를 통해 실시간으로 많은 사용자가 참여하는 경매 환경에서 성능과 안정성을 확보할 수 있었습니다.

   

<br>

2. **쿠폰 발급 동시성 처리 (분산락 및 Lua 스크립트 적용)**
    - **Redisson**을 사용해 **분산락(Distributed Lock)**을 구현하여 다수의 사용자가 동시에 쿠폰을 발급 요청할 때 발생할 수 있는 동시성 문제를 해결했습니다.
    - **Lua 스크립트**를 활용하여 Redis에 저장된 쿠폰 수량을 원자적으로 조회, 업데이트하며 동시성을 제어했습니다.
        - 하나의 스레드가 쿠폰 발급을 처리하는 동안 다른 스레드가 같은 쿠폰 데이터에 접근하지 못하도록 제한하기 위해 **Lock**을 사용했습니다.
        - **쿠폰 발급**과 같은 추가 작업은 Kafka를 통해 **비동기적으로 처리**하여 성능을 최적화했습니다.
    - **도입 결과**:
        - 여러 사용자가 동시에 쿠폰을 발급받으려 시도하더라도 동시성 제어에 성공하여 정해진 수량만큼 정상적인 개수의 쿠폰이 발급됨을 확인했습니다.

<div align="center">
  <img src="./image/coupon1.png" width="45%" style="display: inline-block; margin-right: 10px;">
  <img src="./image/coupon2.png" width="40%" style="display: inline-block;">
</div>


<br>

3. **스프링 배치를 통한 환불 내역 스케줄링 (예외 처리 보완)**
    - 경매가 종료된 후 보증금을 환불해야 하는 사용자를 대상으로 **스프링 배치(Spring Batch)**를 활용하여 환불 내역을 자동으로 처리합니다.
    - 스케줄링을 통해 주기적으로 환불을 처리하고, 예외 발생 시 재처리 로직을 추가하여 시스템 안정성을 강화했습니다.

---

### 상세 기능

- **회원 관리**: 사용자 가입, 로그인 (소셜 로그인 지원), 프로필 관리
- **실시간 경매 및 입찰 기능**: 실시간 입찰, 자동 연장 기능, 낙찰 후 알림 전송
- **알림 시스템**: 입찰 발생 및 경매 종료 시 실시간 알림 전송
- **검색 및 필터링**: 카테고리, 가격, 마감 시간별 필터링 제공
- **결제 시스템**: 낙찰된 물품에 대한 안전 결제 기능 제공