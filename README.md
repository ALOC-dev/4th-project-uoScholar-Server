# UOS Scholar Server

## 프로젝트 개요
서울시립대학교 공지사항을 통합 수집·검색·추천하는 Spring Boot 백엔드 애플리케이션입니다. 공지 크롤러가 주기적으로 게시판을 순회해 `Notice` 엔티티를 갱신하고, 검색 API는 카테고리/키워드 조건으로 목록을 제공하며 HOT 섹션은 최근 게시물 중 조회수 상위 3건을 노출합니다. 또한 검색 키워드를 집계해 인기어를 노출하고, 외부 AI 추천 서버와 연동된 챗봇 엔드포인트도 제공합니다.【F:src/main/java/uos/aloc/scholar/crawler/service/UosViewCountCrawler.java†L27-L116】【F:src/main/java/uos/aloc/scholar/search/controller/NoticeSearchController.java†L24-L89】【F:src/main/java/uos/aloc/scholar/search/controller/KeywordStatsController.java†L16-L35】【F:src/main/java/uos/aloc/scholar/chatting/controller/ChatController.java†L15-L39】

## 기술 스택
- **Java 17** 및 **Spring Boot 3.4** 기반 백엔드.【F:build.gradle†L1-L33】
- **Spring Data JPA**로 MySQL 등 관계형 데이터베이스에 매핑.【F:build.gradle†L23-L32】
- **Jsoup**로 웹 페이지 파싱 후 조회수 정보를 추출.【F:src/main/java/uos/aloc/scholar/crawler/service/UosViewCountCrawler.java†L33-L112】
- **Lombok**으로 DTO/엔티티 보일러플레이트 최소화.【F:build.gradle†L23-L32】

## 주요 모듈
### 검색
- `NoticeSearchController`는 `/notices/search` 요청을 받아 페이지네이션된 공지 목록과 HOT 섹션을 함께 반환합니다.【F:src/main/java/uos/aloc/scholar/search/controller/NoticeSearchController.java†L32-L89】
- `SearchRequestDTO`는 키워드, 카테고리, 페이지 정보와 `exact` 옵션을 정규화하여 검색 조건을 구성합니다.【F:src/main/java/uos/aloc/scholar/search/dto/SearchRequestDTO.java†L15-L42】
- `NoticeSearchServiceImpl`은 `NoticeSearchRepository`의 JPQL 검색 메서드를 호출해 결과를 DTO로 변환합니다.【F:src/main/java/uos/aloc/scholar/search/service/NoticeSearchServiceImpl.java†L15-L27】【F:src/main/java/uos/aloc/scholar/search/repository/NoticeSearchRepository.java†L14-L34】

### HOT 노출
- `HotSearchProperties`는 `search.hot.lookback-days` 설정을 통해 HOT 섹션에 포함할 최대 경과 일수를 주입합니다.【F:src/main/java/uos/aloc/scholar/search/config/HotSearchProperties.java†L7-L22】
- `ClockConfig`에서 공용 `Clock` 빈을 `Asia/Seoul` 타임존으로 등록해 날짜 계산을 일관되게 수행합니다.【F:src/main/java/uos/aloc/scholar/config/ClockConfig.java†L5-L18】
- `NoticeSearchRepository`는 단일 또는 다중 카테고리에서 최근 게시물만을 대상으로 조회수 상위 3건을 반환하는 파생 쿼리를 제공합니다.【F:src/main/java/uos/aloc/scholar/search/repository/NoticeSearchRepository.java†L26-L34】

### 키워드 통계
- `KeywordStatsService`는 검색 키워드 로그를 저장하고 최근 N일 인기어 상위 K건을 계산합니다.【F:src/main/java/uos/aloc/scholar/search/service/KeywordStatsService.java†L19-L61】
- `KeywordStatsController`는 `/search/popular` 엔드포인트에서 인기어를 순번별로 반환합니다.【F:src/main/java/uos/aloc/scholar/search/controller/KeywordStatsController.java†L16-L35】
- `SearchKeywordDaily` 엔티티는 키워드·일자별 집계 테이블을 정의하며, 중복 방지를 위해 `(keyword, day)` 유니크 제약을 가집니다.【F:src/main/java/uos/aloc/scholar/search/entity/SearchKeywordDaily.java†L7-L28】

### 조회수 크롤러
- `UosViewCountCrawler`는 게시판 HTML을 파싱해 페이지별 게시물 번호와 조회수를 추출하고, 데이터베이스와 비교 후 변경된 항목만 업데이트합니다.【F:src/main/java/uos/aloc/scholar/crawler/service/UosViewCountCrawler.java†L27-L206】
- `NoticeRepository`는 조회수 일괄 갱신을 위한 `updateViewCount` 쿼리와 페이지 내 게시물 사전 조회 메서드를 제공합니다.【F:src/main/java/uos/aloc/scholar/crawler/repository/NoticeRepository.java†L16-L28】
- `ViewCountCrawlJob`은 3시간마다 전체 게시판 동기화를 트리거하며, `crawler.view-sync.pages` 설정으로 탐색 페이지 수를 조절합니다.【F:src/main/java/uos/aloc/scholar/crawler/schedule/ViewCountCrawlJob.java†L19-L30】
- `CrawlerController`는 관리자용 수동 동기화 엔드포인트(`/admin/view-sync/*`)를 제공합니다.【F:src/main/java/uos/aloc/scholar/crawler/controller/CrawlerController.java†L14-L31】

### AI 챗봇 연동
- `ChatController`는 `/chat/ai` 요청을 받아 AI 서버의 검색 결과를 전달합니다.【F:src/main/java/uos/aloc/scholar/chatting/controller/ChatController.java†L15-L39】
- `AIService`는 `RestTemplate`을 사용해 외부 AI 서버의 `/search` 엔드포인트를 호출하고 응답을 `NoticeDTO` 리스트로 변환합니다.【F:src/main/java/uos/aloc/scholar/chatting/service/AIService.java†L21-L50】
- `RestTemplateConfig`는 `RestTemplate` 빈을 구성해 서비스에 주입합니다.【F:src/main/java/uos/aloc/scholar/chatting/config/RestTemplateConfig.java†L5-L13】

## 도메인 모델
- `Notice` 엔티티는 카테고리, 게시글 번호, 제목, 요약, 게시일, 부서, 조회수 등을 저장하며 `(category, post_number)`에 유니크 제약, `(category, posted_date, id)` 인덱스를 정의합니다.【F:src/main/java/uos/aloc/scholar/crawler/entity/Notice.java†L7-L74】
- `NoticeCategory` 열거형은 일반/학사 공지 및 각 단과대 게시판을 표현합니다.【F:src/main/java/uos/aloc/scholar/crawler/entity/NoticeCategory.java†L1-L13】

## API 요약
| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/notices/search` | 키워드·카테고리 검색 결과와 HOT 리스트를 반환합니다. `exact=true`이고 첫 페이지에서 키워드가 비어 있을 때 HOT 섹션이 활성화됩니다.【F:src/main/java/uos/aloc/scholar/search/controller/NoticeSearchController.java†L32-L74】 |
| `GET` | `/search/popular` | 최근 N일 인기 검색어를 순번별(`keyword1`~)로 응답합니다.【F:src/main/java/uos/aloc/scholar/search/controller/KeywordStatsController.java†L16-L35】 |
| `POST` | `/admin/view-sync/all` | 모든 게시판에 대해 조회수 동기화를 실행합니다.【F:src/main/java/uos/aloc/scholar/crawler/controller/CrawlerController.java†L14-L23】 |
| `POST` | `/admin/view-sync/general` | 일반 공지 게시판만 조회수 동기화를 수행합니다.【F:src/main/java/uos/aloc/scholar/crawler/controller/CrawlerController.java†L25-L31】 |
| `POST` | `/chat/ai` | 사용자의 메시지를 AI 서버에 전달하고 추천 공지를 반환합니다.【F:src/main/java/uos/aloc/scholar/chatting/controller/ChatController.java†L21-L39】 |

`SearchResponseDTO` 응답은 `hot`, `content`, 페이지 정보(`page`, `size`, `totalElements` 등)를 포함합니다.【F:src/main/java/uos/aloc/scholar/search/dto/SearchResponseDTO.java†L5-L17】 `NoticeResponseDTO`는 공지 ID, 제목, 게시일, 부서, 링크, 카테고리, 조회수 필드를 제공합니다.【F:src/main/java/uos/aloc/scholar/search/dto/NoticeResponseDTO.java†L6-L26】

## 스케줄링 및 크롤링 동작
- `@EnableScheduling` 구성(`SchedulingConfig`)이 활성화되어 있으며, `ViewCountCrawlJob`은 기본적으로 3시간 간격(`0 0 */3 * * *`)으로 실행됩니다.【F:src/main/java/uos/aloc/scholar/crawler/config/SchedulingConfig.java†L5-L11】【F:src/main/java/uos/aloc/scholar/crawler/schedule/ViewCountCrawlJob.java†L19-L26】
- 크롤러는 페이지별로 `ViewEntry` 리스트를 구성한 뒤 DB 조회 결과와 비교해 존재하지 않는 게시물에서는 miss streak를 증가시키고, 조회수가 변한 게시물만 갱신해 불필요한 UPDATE를 줄입니다.【F:src/main/java/uos/aloc/scholar/crawler/service/UosViewCountCrawler.java†L117-L206】

## 환경 설정
다음 속성은 `application.yml` 또는 환경 변수로 조정할 수 있습니다.

| 프로퍼티 | 기본값 | 설명 |
| --- | --- | --- |
| `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` | 없음 | MySQL 등 JPA 연결 정보 (필수). |
| `crawler.view-sync.pages` | `10` | 각 게시판에서 확인할 페이지 수.【F:src/main/java/uos/aloc/scholar/crawler/schedule/ViewCountCrawlJob.java†L19-L24】 |
| `crawler.view-sync.cron` | `0 0 */3 * * *` | 조회수 동기화 작업의 실행 주기.【F:src/main/java/uos/aloc/scholar/crawler/schedule/ViewCountCrawlJob.java†L21-L26】 |
| `search.hot.lookback-days` | `30` | HOT 섹션에 포함할 최대 경과 일수.【F:src/main/java/uos/aloc/scholar/search/config/HotSearchProperties.java†L7-L22】 |
| `ai.server.url` | `https://5000-alocdev-3rdprojectuosch-3ihm59nyj5t.ws-us120.gitpod.io` | AI 추천 서버 엔드포인트 기본값.【F:src/main/java/uos/aloc/scholar/chatting/service/AIService.java†L25-L29】 |

## 로컬 실행 방법
1. **사전 준비**: JDK 17 이상과 MySQL 데이터베이스를 설치합니다. 애플리케이션이 사용할 DB와 계정을 생성하세요.【F:build.gradle†L10-L13】
2. **환경 구성**: `src/main/resources/application.yml`을 생성해 데이터소스 및 위 설정값을 정의합니다 (예시는 아래 참고).
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/uo_scholar?useSSL=false&serverTimezone=Asia/Seoul
       username: root
       password: secret
     jpa:
       hibernate:
         ddl-auto: update
       properties:
         hibernate:
           format_sql: true
   search:
     hot:
       lookback-days: 30
   crawler:
     view-sync:
       pages: 10
       cron: "0 0 */3 * * *"
   ai:
     server:
       url: https://example.com
   ```
3. **빌드 및 실행**:
   - 종속성 설치 및 빌드: `./gradlew build`
   - 개발 서버 실행: `./gradlew bootRun`
4. **테스트 데이터 수집**: 애플리케이션을 기동한 뒤 자동 스케줄러 또는 `/admin/view-sync/all` 호출로 초기 조회수를 동기화합니다.【F:src/main/java/uos/aloc/scholar/crawler/controller/CrawlerController.java†L14-L23】

## 추가 가이드
- 메인 클래스 `ScholarApplication`을 통해 애플리케이션을 시작하며, 스케줄러와 REST 컨트롤러 빈이 자동으로 등록됩니다.【F:src/main/java/uos/aloc/scholar/ScholarApplication.java†L1-L14】
- `NoticeCategory` 열거형을 확장하면 크롤러 및 검색 로직에도 동일하게 반영되므로, 새로운 게시판을 지원할 때는 `syncAllBoards`에 해당 게시판 호출을 추가하세요.【F:src/main/java/uos/aloc/scholar/crawler/service/UosViewCountCrawler.java†L37-L49】【F:src/main/java/uos/aloc/scholar/crawler/entity/NoticeCategory.java†L1-L13】
- 외부 AI 서버가 응답하지 않을 경우 `AIService`는 오류를 로깅하고 `null` 응답을 반환하므로, 프런트엔드에서 예외 처리를 해주는 것이 좋습니다.【F:src/main/java/uos/aloc/scholar/chatting/service/AIService.java†L33-L49】
