# 미니 쇼핑몰

Spring Boot + PostgreSQL + Redis + Elasticsearch + React로 구성된 간단한 쇼핑몰

## 기술 스택

### Backend
- **Spring Boot 3.2.0** - 애플리케이션 프레임워크
- **PostgreSQL** - 메인 데이터베이스 (상품, 주문 정보)
- **Redis** - 캐싱 및 장바구니 세션 저장
- **RabbitMQ** - 비동기 메시징 및 이벤트 기반 아키텍처
- **Elasticsearch** - 상품 검색
- **Spring Data JPA** - ORM
- **Lombok** - 코드 간소화

### ELK Stack (로그 관리)
- **Elasticsearch** - 로그 저장 및 검색
- **Logstash** - 로그 수집 및 변환
- **Kibana** - 로그 시각화 및 모니터링

### Frontend
- **React 18** - UI 라이브러리
- **JavaScript (ES6+)** - 프로그래밍 언어
- **CSS3** - 스타일링

## 주요 기능

1. **상품 관리**
    - 상품 목록 조회
    - 상품 상세 정보
    - 카테고리별 필터링

2. **검색 (Elasticsearch)**
    - 상품명/설명 기반 전문 검색
    - 실시간 검색 결과

3. **장바구니 (Redis)**
    - 장바구니 담기
    - 수량 변경
    - 상품 삭제
    - 24시간 자동 만료

4. **최근 본 상품 (Redis)**
    - 상품 조회 시 자동 저장
    - 최대 10개까지 보관 (최신순)
    - Redis ZSet 활용 (timestamp score)
    - 30일 자동 만료
    - 개별 삭제 및 전체 삭제 기능

5. **주문**
    - 장바구니에서 주문 생성
    - 주문 내역 조회
    - 재고 자동 차감

6. **캐싱 (Redis)**
    - 상품 목록 캐싱 (10분)
    - 조회 성능 최적화

## 프로젝트 구조

```
mini-shop/
├── src/
│   └── main/
│       ├── java/com/example/minishop/
│       │   ├── config/          # 설정 클래스
│       │   ├── controller/      # REST API 컨트롤러
│       │   ├── service/         # 비즈니스 로직
│       │   ├── repository/      # 데이터 액세스
│       │   ├── entity/          # JPA 엔티티
│       │   ├── document/        # Elasticsearch 문서
│       │   ├── model/           # DTO 모델
│       │   └── dto/             # 요청/응답 DTO
│       └── resources/
│           └── application.yml  # 설정 파일
│
├── frontend/                    # React 프론트엔드
│   ├── public/
│   └── src/
│       ├── components/          # React 컴포넌트
│       ├── App.jsx
│       ├── App.css
│       └── index.js
│
├── pom.xml                      # Maven 의존성
├── docker-compose.yml           # Docker 설정
└── README.md
```

## 실행 방법

### 1. 사전 요구사항
- Java 17 이상
- Node.js 14 이상
- Docker & Docker Compose
- Maven

### 2. 인프라 실행 (Docker)

```bash
# 디렉토리 생성
mkdir -p logstash/config logstash/pipeline logs

# PostgreSQL, Redis, RabbitMQ, Elasticsearch, Logstash, Kibana 실행
docker-compose up -d

# 컨테이너 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

**접속 확인:**
- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601
- Logstash: http://localhost:9600
- **RabbitMQ Management**: http://localhost:15672 (rabbitmq/rabbitmq)

### 3. Backend 실행

```bash
# Maven으로 빌드 및 실행
mvn clean install
mvn spring-boot:run

# 또는 직접 실행
java -jar target/mini-shop-0.0.1-SNAPSHOT.jar
```

서버가 `http://localhost:8080`에서 실행됩니다.

### 4. Frontend 실행

```bash
# frontend 디렉토리로 이동
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm start
```

애플리케이션이 `http://localhost:3000`에서 실행됩니다.

## API 엔드포인트

### 상품 API
- `GET /api/products` - 전체 상품 목록
- `GET /api/products/{id}` - 상품 상세
- `POST /api/products` - 상품 생성
- `PUT /api/products/{id}` - 상품 수정
- `DELETE /api/products/{id}` - 상품 삭제
- `GET /api/products/search?keyword={keyword}` - 상품 검색
- `GET /api/products/category/{category}` - 카테고리별 조회

### 장바구니 API
- `POST /api/cart` - 장바구니 추가
- `GET /api/cart/{userId}` - 장바구니 조회
- `PUT /api/cart` - 장바구니 수량 변경
- `DELETE /api/cart/{userId}/{productId}` - 상품 삭제
- `DELETE /api/cart/{userId}` - 장바구니 비우기

### 최근 본 상품 API
- `POST /api/recent-views?userId={userId}&productId={productId}` - 최근 본 상품 추가
- `GET /api/recent-views/{userId}` - 최근 본 상품 목록
- `GET /api/recent-views/{userId}/count` - 최근 본 상품 개수
- `GET /api/recent-views/{userId}/ids` - 최근 본 상품 ID 목록
- `DELETE /api/recent-views/{userId}/{productId}` - 특정 상품 삭제
- `DELETE /api/recent-views/{userId}` - 전체 삭제

### 주문 API
- `POST /api/orders` - 주문 생성
- `GET /api/orders/{id}` - 주문 상세
- `GET /api/orders/customer/{email}` - 고객별 주문 조회
- `GET /api/orders` - 전체 주문 조회
- `PATCH /api/orders/{id}/status?status={status}` - 주문 상태 변경

## 데이터베이스 접속 정보

### PostgreSQL
- Host: localhost:5432
- Database: shopdb
- Username: postgres
- Password: postgres

### Redis
- Host: localhost:6379

### Elasticsearch
- URL: http://localhost:9200

## 초기 데이터

애플리케이션 실행 시 자동으로 10개의 샘플 상품이 생성됩니다:
- 전자기기 (맥북, 아이폰, 에어팟 등)
- 의류 (패딩, 트레이닝복)
- 신발 (나이키, 뉴발란스)

## 주요 특징

### 1. 캐싱 전략
- Redis를 사용한 상품 목록 캐싱
- 10분 TTL 설정
- 상품 변경 시 자동 캐시 무효화
- Jackson ObjectMapper를 사용한 LocalDateTime 직렬화 지원

### 2. 검색 최적화
- Elasticsearch를 통한 전문 검색
- 상품명과 설명에서 동시 검색
- 한글 검색 지원

### 3. 장바구니 관리
- Redis Hash 자료구조 활용
- 24시간 자동 만료
- 실시간 수량 변경

### 4. 최근 본 상품
- Redis ZSet 자료구조 활용
- Timestamp를 score로 사용하여 자동 정렬
- 최대 10개까지 저장 (초과 시 가장 오래된 항목 자동 삭제)
- 30일 TTL 설정
- 중복 조회 시 timestamp만 업데이트

### 5. 재고 관리
- 주문 시 자동 재고 차감
- 재고 부족 시 주문 불가
- 트랜잭션 보장

## 트러블슈팅

### LocalDateTime 직렬화 오류
Redis에 저장할 때 LocalDateTime 직렬화 오류가 발생하면:
```java
// RedisConfig에서 JavaTimeModule 등록 확인
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

엔티티에 @JsonFormat 어노테이션 추가:
```java
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
private LocalDateTime createdAt;
```

### Elasticsearch 연결 오류
```bash
# Elasticsearch 상태 확인
curl http://localhost:9200

# 컨테이너 재시작
docker-compose restart elasticsearch
```

### Redis 연결 오류
```bash
# Redis 연결 테스트
docker exec -it shop-redis redis-cli ping

# 응답: PONG
```

### PostgreSQL 연결 오류
```bash
# PostgreSQL 접속 테스트
docker exec -it shop-postgres psql -U postgres -d shopdb
```

### 1. 로그 확인
```bash
# Docker 로그
docker-compose logs -f

# Spring Boot 로그
tail -f logs/spring-boot.log
```

### 2. 데이터 초기화
```bash
# 모든 데이터 삭제 및 재생성
docker-compose down -v
docker-compose up -d
mvn spring-boot:run
```

## 개선 가능한 부분

- [ ] 사용자 인증/인가 (Spring Security + JWT)
- [ ] 상품 이미지 업로드
- [ ] 결제 기능
- [ ] 주문 상태 추적
- [ ] 관리자 페이지
- [ ] 페이지네이션
- [ ] 리뷰 시스템
- [ ] 위시리스트
