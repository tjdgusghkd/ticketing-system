# Ticketing System
  > Redis 기반 대기열과 좌석 선점 제어를 통해 **동일 좌석 중복 예매 문제를 해결하는 티켓팅 시스템**

  [![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](#)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-6DB33F?logo=springboot&logoColor=white)](#)
  [![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)](#)
  [![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate&logoColor=white)](#)
  [![Thymeleaf](https://img.shields.io/badge/Thymeleaf-SSR-005F0F?logo=thymeleaf&logoColor=white)](#)
  [![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](#)
  [![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis&logoColor=white)](#)
  [![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](#)
  [![AWS](https://img.shields.io/badge/AWS-EC2%20%2B%20RDS-FF9900?logo=amazonaws&logoColor=white)](#)
  [![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?logo=gradle&logoColor=white)](#)

  콘서트 예매 상황을 가정하여, 사용자가 공연과 회차를 조회하고 좌석을 선택하여 예매할 수 있는 티켓팅 시스템입니다.

  이 프로젝트는 단순 CRUD 구현보다 다음 문제를 직접 설계하고 검증하는 데 집중했습니다.

  - 동일 좌석 동시 접근 시 중복 예매 방지
  - Redis 기반 대기열 제어
  - 좌석 선점 및 만료 처리
  - 부하 테스트 기반 병목 분석 및 리팩토링
  - Docker 기반 배포 및 배포 환경 검증

  ---

  ## 1. 프로젝트 소개

  기존 프로젝트들에서 일반적인 CRUD와 웹 서비스 흐름을 구현했다면, 
  이번 프로젝트는 티켓팅 시스템의 핵심 문제인 **동시성 제어**와 **데이터 정합성*에 집중했습니다.

  특히 다음을 직접 다뤘습니다.

  - 동일 좌석에 대한 동시 선점 충돌
  - 대기열 기반 좌석 페이지 접근 제어
  - Redis와 RDB 역할 분리
  - Lua Script를 활용한 원자적 처리
  - k6 기반 부하 테스트와 리팩토링 효과 검증
  - EC2 + RDS 기반 배포 환경 구성

  ---

  ## 2. 핵심 기능

  ### 사용자 기능
  - 회원가입
  - 로그인 / 로그아웃
  - 공연 목록 조회
  - 공연 상세 조회
  - 회차별 좌석 조회
  - 예매 내역 조회

  ### 티켓팅 핵심 기능
  - Redis 기반 대기열 진입 및 승격
  - 좌석 페이지 접근 제어
  - Redis 기반 좌석 선점 및 TTL 만료 처리
  - 동일 좌석 중복 선점 방지
  - 예약 처리 및 정합성 보장
  - 부하 테스트 기반 성능 개선

  ---

  ## 3. 기술 스택

  ### Backend
  - Java 21
  - Spring Boot 3.5.11
  - Spring Security
  - Spring Data JPA
  - Thymeleaf
  - Gradle

  ### Database / Cache
  - MySQL 8.0
  - Redis

  ### Infra
  - Docker
  - Docker Compose
  - AWS EC2
  - AWS RDS
  - Nginx

  ### Test
  - JUnit
  - k6

  ### Tool
  - Git / GitHub
  - STS4
  - MySQL Workbench

  ---

  ## 4. 핵심 설계 포인트

  ### 1) 공연과 회차 분리
  하나의 공연은 여러 회차를 가질 수 있으므로
  `CONCERT`와 `CONCERT_SCHEDULE`을 분리하여 관리합
  니다.

  예)
  - 아이유 콘서트
    - 1회차 : 2026-05-01 19:00
    - 2회차 : 2026-05-02 19:00

  ### 2) 좌석과 회차별 좌석 분리
  물리적인 좌석(`SEAT`)과 특정 회차의 좌석 (`SCHEDULE_SEAT`)을 분리했습니다.

  같은 좌석이라도 회차마다 상태가 다를 수 있기 때문입니다.

  예)
  - A1 좌석
    - 1회차 : 예약 완료
    - 2회차 : 예매 가능

  ### 3) 선점은 Redis, 최종 예약은 RDB
  - **Redis**: 좌석 선점 상태와 TTL 관리
  - **MySQL**: 최종 예약 정보 저장

  즉,
  - 선점 상태는 Redis에서 빠르게 관리
  - 실제 예약 완료 여부는 DB를 기준으로 관리

  ### 4) 예약 대상은 `SCHEDULE_SEAT`
  실제 예약 단위를 `SCHEDULE_SEAT`로 두어 **특정회차의 특정 좌석**을 명확하게 식별하도록 설계했습니다.

  ### 5) 대기열은 Redis active / wait 구조로 분리
  회차별 대기열은 아래 Redis 구조로 관리합니다.

  - `active:round:{scheduleNo}` : 현재 좌석 페이지 접근이 허용된 사용자
  - `wait:round:{scheduleNo}` : 대기 사용자 목록
  - `queue:hb:{scheduleNo}:{loginId}` : heartbeatkey

  또한,
  - heartbeat 기반 stale user 정리
  - Lua Script 기반 대기열 진입 / 승격 원자화 구조를 적용했습니다.

  ---

  ## 5. 예매 처리 흐름

  ```text
Login
  -> Queue Enter
  -> Queue Active
  -> Seat Page Access
  -> Seat Hold
  -> Reserve

  상세 흐름:

  1. 사용자가 공연과 회차를 선택한다.
  2. 대기열에 진입한다.
  3. active 사용자만 좌석 페이지에 접근한다.
  4. 좌석 선택 시 Redis에 선점 정보를 저장한다.
  5. 선점 시간 내 예매를 완료하면 예약 정보를 DB에 저장한다.
  6. 예약 완료 후 Redis 선점 정보를 정리한다.
```
  ---

  ## 6. 동시성 제어 전략

  티켓팅 시스템에서는 여러 사용자가 동시에 동일 좌석을 예매하려고 시도할 수 있습니다.
  이 문제를 해결하기 위해 다음 전략을 적용했습니다.

  - Redis 기반 좌석 선점 (TTL 5분)
  - 동일 좌석 중복 요청 시 선점 상태 확인 후 차단
  - 좌석 선점 로직의 Redis 조회/검사/저장 구간 Lua 원자화
  - 대기열 active 사용자만 좌석 API 접근 허용
  - 예약 처리 시 트랜잭션과 Redis 검증/정리 순서 분리
  - Redis를 활용해 DB 부하를 줄이고 빠른 선점 처리 수행

  ---

  ## 7. 주요 리팩토링

  ### Queue

  - checkQueue()에서 stale cleanup 제거
  - queue-status.lua를 조회 전용(read-only) 구조로 분리
  - stale cleanup을 @Scheduled 기반으로 분리
  - active:schedules 도입
  - 대기열 승격 로직을 Lua로 원자화
  - Redis Lua Script 객체 재사용 구조로 변경

  ### Seat Hold

  - holdSeat() 내부 Redis 구간을 Lua로 이동
  - GET -> conflict 검사 -> SET -> SADD 흐름 원자화
  - cleanupAndCountUserHolds()를 Lua로 이동해 추가 개선 시도
  - 좌석 페이지 heartbeat 유지 및 polling 반영

  ### Reserve

  - 반복 조회/반복 저장 구조 정리
  - findAllById + saveAll 구조로 변경
  - 예약 전 Redis hold 검증과 예약 후 cleanup 분리
  - 검증용 Lua / cleanup용 Lua 분리로 정합성 개선

  ---

  ## 8. 부하 테스트 결과 요약

  ### Queue

  - 700 VUs까지는 안정적으로 처리
  - 1000 VUs는 경계 구간
  - 1500 ~ 2000 VUs 구간에서 실패율 증가 및 연결 수용 한계 확인

  ### Seat Hold

  - baseline은 100 VUs부터 급격히 응답 지연 증가
  - holdSeat() Redis Lua화 이후 응답시간과 처리량이 크게 개선
  - cleanupAndCountUserHolds() Lua화는 일부 구간 개선, 일부 구간 악화로 일관된 개선은 아님

  ### 종합 결론

  - 설정 튜닝보다 코드 리팩토링 효과가 더 크게 확인되었다
  - 특히 Queue와 Seat Hold에서 Redis 구간 구조 개선이 핵심이었다

  자세한 내용은 아래 문서를 참고했습니다.

  - docs/queue-load-test-summary.md
  - docs/seat-hold-load-test-summary.md

  ---

  ## 9. 배포 환경

  현재 프로젝트는 아래 구조로 배포 및 검증했습니다.

  - EC2
      - Spring Boot Application
      - Redis
      - Nginx
  - RDS
      - MySQL
  - Docker Compose
      - 애플리케이션 및 인프라 컨테이너 관리

  배포 환경에서 다음 이슈들도 직접 점검했습니다.

  - Linux 환경에서 정적 리소스 파일명 대소문자 문
    제
  - queue -> seat 이동 시 leave 호출 타이밍 문제
  - seat 페이지 heartbeat 누락으로 인한 권한 상실 문제
  - polling을 통한 좌석 상태 동기화

  ---

  ## 10. ERD

  주요 테이블

  - MEMBER
  - CONCERT
  - CONCERT_SCHEDULE
  - SEAT
  - SCHEDULE_SEAT
  - RESERVATION

  ```mermaid
  erDiagram
      MEMBER ||--o{ RESERVATION : makes
      CONCERT ||--o{ CONCERT_SCHEDULE : has
      CONCERT_SCHEDULE ||--o{ SCHEDULE_SEAT : contains
      SEAT ||--o{ SCHEDULE_SEAT : provides
      RESERVATION ||--|| SCHEDULE_SEAT : reserves
  ```
  ---

  ## 11. Redis Key Design

  | Key Pattern | Type | Description | TTL |
  |---|---|---|---|
  | active:round:{scheduleNo} | Set | 현재 좌석 페이지 접근이 허용된 사용자 목록 | 없음 |
  | wait:round:{scheduleNo} | Sorted Set | 회차별 대기열 사용자 목록 | 없음 |
  | queue:hb:{scheduleNo}:{loginId} | String | active / wait 사용자의 heartbeat | 30초 |
  | seat:hold:{scheduleNo}:{seatNo} | String | 특정 좌석을 선점한 사용자 loginId | 5분 |
  | user:hold:{scheduleNo}:{loginId} | Set | 사용자가 선점한 좌석 목록 | 없음 |
  | RT:{loginId} | String | 사용자 Refresh Token | 토큰 만료 시간 기준 |

  ---
  
  ## 12. 프로젝트 구조

```bash
  src/main/java/com/ticketing
   ├─ global
   │   ├─ config
   │   ├─ exception
   │   ├─ jwt
   │   └─ util
   │
   ├─ member
   │   ├─ controller
   │   ├─ dto
   │   ├─ entity
   │   ├─ repository
   │   └─ service
   │
   ├─ concert
   │   ├─ controller
   │   ├─ dto
   │   ├─ entity
   │   ├─ repository
   │   └─ service
   │
   ├─ seat
   │   ├─ controller
   │   ├─ dto
   │   ├─ entity
   │   ├─ repository
   │   └─ service
   │
   ├─ queue
   │   ├─ controller
   │   ├─ dto
   │   └─ service
   │
   └─ reservation
       ├─ controller
       ├─ dto
       ├─ entity
       ├─ repository
       └─ service
```
  ---

  ## 13. 트러블슈팅

  - queue -> seat 이동 시 leaveQueueOnExit()가 먼저 호출되어 active 권한이 사라지던 문제 수정
  - seat 페이지에서 heartbeat 누락으로 일정 시간 후 권한 상실 및 반복 alert가 발생하던 문제 수정
  - 정적 리소스 파일명 대소문자 차이로 배포 환경에서 CSS가 깨지던 문제 수정
  - 409 에러 응답이 JSON 전체로 alert에 노출되던문제 수정
  - 좌석 선점 상태가 다른 사용자 화면에 즉시 반영되지 않아 polling 방식으로 동기화

  ---

  ## 14. 진행 현황

  - [x] 프로젝트 초기 세팅 및 ERD 설계
  - [x] 공연/회차/좌석 조회 API 구현
  - [x] Spring Security + JWT 기반 인증 시스템
  - [x] Redis 기반 좌석 선점 로직 구현
  - [x] Redis Sorted Set 기반 대기열 시스템 구현
  - [x] Lua Script를 활용한 대기열 진입/승격 원자성 보장
  - [x] 좌석 선점 Redis 로직 Lua 원자화
  - [x] 예약 로직 구조 정리 및 정합성 개선
  - [x] k6 기반 부하 테스트 및 성능 검증
  - [x] Docker 기반 EC2 배포
  - [x] RDS 연동 및 배포 환경 수동 검증
  - [ ] 도메인 및 SSL 적용
  - [ ] 예매 취소 및 좌석 환원 로직

  ---

  ## 15. 회고

  이 프로젝트에서는 일반적인 CRUD 기능 확장보다,
  실제 티켓팅 시스템에서 중요한 문제인 동시성, 정합성, 성능, 배포 환경 차이를 직접 다뤘습니다.

  특히 다음을 배웠습니다.

  - 코드 구조 개선이 단순 설정 튜닝보다 더 큰 성능 개선을 만들 수 있음
  - Redis와 RDB를 역할에 맞게 분리하는 것이 중요함
  - 로컬 환경과 Linux 배포 환경은 정적 리소스, 인증 흐름, 네트워크 측면에서 차이가 큼
  - 부하 테스트 결과를 기반으로 리팩토링 방향을 검증하는 과정이 중요함
