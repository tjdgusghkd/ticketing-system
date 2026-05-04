 # 부하 테스트 리포트: 대기열(Queue) 성능 개선 및 한계 분석

  ## 1. 개요
  - **테스트 일시:** 2026-05-04
  - **대상 기능:** `queue enter`, `queue status`, `queue heartbeat`
  - **핵심 API:** `GET /api/queue/{scheduleNo}/status`
  - **테스트 목적:** 대기열 구조 리팩토링 이후 응
  답 성능, 실패율, CPU 사용률 변화를 확인하고 단
  일 인스턴스 기준 임계 구간을 파악

  ## 2. 테스트 환경
  - **Server:** Localhost (Spring Boot 3.x, Java 17)
  - **Database:** Redis
  - **Tool:** k6
  - **Monitoring:** Windows 작업 관리자, PowerShell CPU 샘플링

  ## 3. 리팩토링 내용
  ### 3.1. Queue 로직 개선
  - `checkQueue()`에서 stale cleanup 제거
  - `queue-status.lua`를 조회 전용(read-only) 구조로 분리
  - stale cleanup을 `@Scheduled` 기반으로 분리
  - `active:schedules` 집합을 도입해 cleanup 대상 회차를 관리
  - 승격 로직(`wait -> active`)을 별도 Lua 스크립트로 원자화
  - Redis Lua script 객체를 요청마다 생성하지 않고 재사용하도록 수정

  ### 3.2. 테스트/설정 정리
  - `QueueServiceIntegrationTest`를 현재 구조에 맞게 갱신
  - 톰캣 thread / connection 설정 추가
  - HikariCP 설정 명시

  ## 4. 주요 부하 테스트 결과

  | 구간 | 설정 | TPS | p95 | CPU max | 실패율 | 해석 |
  | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
  | 안정 구간 | 700 VUs / 1m / sleep 2s | 330.70/s | 125.72ms | 40% | 0.00% | 안정적으로 처리 |
  | 경계 구간 A | 1000 VUs / 1m / sleep 2s | 474.67/s | 108.34ms | 47% | 0.00% | 정상 처리 사례 확인 |
  | 경계 구간 B | 1000 VUs / 1m / sleep 2s | 472.67/s | 185.69ms | 미측정 | 3.68% | `connect refused` 재현 |
  | 한계 구간 A | 1500 VUs / 1m / sleep 2s | 704.88/s | 205.40ms | 52.29% | 4.47% | 한계 구간 진입 |
  | 한계 구간 B | 2000 VUs / 1m / sleep 2s | 933.17/s | 232.71ms | 70.90% | 5.27% | 연결 수용 한계 확인 |

  ## 5. 리팩토링 이후 변화
  - `queue-status`는 리팩토링 이후 중간 부하 구간에서 안정적으로 동작했다.
  - `700 ~ 1000 VUs` 구간에서는 전반적으로 낮은 실패율과 안정적인 응답 시간을 유지했다.
  - CPU 점유율도 부하 증가에 따라 비교적 완만하게 상승하여, 리팩토링 이후 전반적인 안정화 효과를 확인했다.

  ## 6. 병목 분석
  - `1500 VUs` 이상부터 실패율이 4~5% 수준으로 발생했다.
  - CPU가 완전히 포화되기 전에 `connect refused`가 먼저 관찰되었다.
  - 현재 병목은 애플리케이션 로직 자체보다는 다음 성격에 더 가깝다.
  - 톰캣 연결 수용 한계
  - 로컬 소켓/커넥션 처리 한계
  - 단일 인스턴스 burst 처리 한계

  ## 7. 결론
  - 대기열 구조 리팩토링은 실제 성능 개선 효과가 있었다.
  - `queue-status`는 단일 로컬 인스턴스 기준 `700VUs`까지는 안정적이었다.
  - `1000 VUs`는 경계 구간, `1500 ~ 2000 VUs`는 한계 구간으로 판단된다.
  - 현재 단계에서는 Queue 코드 리팩토링보다 시스템 튜닝 및 다중 인스턴스 확장 검토가 더 적절하다.

  ## 8. 향후 계획
  - `enter + status + heartbeat` 혼합 시나리오 결과를 별도 정리
  - `reserve` 테스트 진행
  - 필요 시 `stages` 기반 점진 증가 테스트 수행
  - 로드 밸런싱 / 다중 인스턴스 환경에서 재측정
