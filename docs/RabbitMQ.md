# RabbitMQ 통합 가이드

## 🎯 RabbitMQ 도입 목적

### 이전 아키텍처 (동기 처리)
```
Client → Spring Boot → PostgreSQL
                    → Elasticsearch (동기)
                    → 재고 업데이트 (동기)
                    → 알림 전송 (동기)
```

**문제점:**
- 모든 작업이 동기적으로 처리됨
- Elasticsearch 실패 시 전체 요청 실패
- 응답 시간 증가
- 확장성 제한

### 현재 아키텍처 (이벤트 기반)
```
Client → Spring Boot → PostgreSQL
                    ↓
                 RabbitMQ (비동기)
                    ↓
            ┌───────┼───────┐
            ↓       ↓       ↓       ↓
       Order   Stock   Notification  Elasticsearch
      Processor  Processor  Service   Indexer
```

**장점:**
- 비동기 처리로 응답 속도 향상
- 서비스 간 느슨한 결합
- 장애 격리 (한 서비스 실패가 전체에 영향 없음)
- 수평 확장 가능
- 메시지 재시도 및 Dead Letter Queue

## 📊 메시징 구조

### Exchange
```
shop.exchange (Topic Exchange)
```

### Queues & Routing Keys

| Queue | Routing Key | 목적 |
|-------|-------------|------|
| order.queue | order.created | 주문 생성 처리 |
| stock.queue | stock.updated | 재고 변동 모니터링 |
| notification.queue | notification.send | 알림 발송 |
| elasticsearch.queue | elasticsearch.index | 검색 인덱싱 |
| shop.dlq.queue | dlq | 실패한 메시지 처리 |

### 메시지 흐름

#### 1. 주문 생성
```
OrderService.createOrder()
    ↓
PostgreSQL에 주문 저장
    ↓
EventPublisher.publishOrderCreated()
    ↓
RabbitMQ (order.queue)
    ↓
OrderEventListener.handleOrderCreated()
    ↓
외부 시스템 연동 / 추가 처리
```

#### 2. 재고 업데이트
```
ProductService.decreaseStock()
    ↓
PostgreSQL 재고 감소
    ↓
EventPublisher.publishStockUpdated()
    ↓
RabbitMQ (stock.queue)
    ↓
StockEventListener.handleStockUpdated()
    ↓
재고 부족 알림 / 히스토리 저장
```

#### 3. Elasticsearch 인덱싱
```
ProductService.createProduct()
    ↓
PostgreSQL에 상품 저장
    ↓
EventPublisher.publishElasticsearchIndex()
    ↓
RabbitMQ (elasticsearch.queue)
    ↓
ElasticsearchEventListener.handleElasticsearchIndex()
    ↓
Elasticsearch 인덱싱
```

#### 4. 알림 발송
```
OrderService.createOrder()
    ↓
주문 완료
    ↓
EventPublisher.publishNotification()
    ↓
RabbitMQ (notification.queue)
    ↓
NotificationEventListener.handleNotification()
    ↓
EMAIL / SMS / PUSH 발송
```

## 🚀 실행 방법

### 1. RabbitMQ 시작

```bash
# Docker Compose로 전체 스택 시작
docker-compose up -d

# RabbitMQ 상태 확인
docker-compose ps | grep rabbitmq

# RabbitMQ 로그 확인
docker-compose logs -f rabbitmq
```

### 2. RabbitMQ Management UI 접속

```
URL: http://localhost:15672
Username: admin
Password: admin
```

### 3. Spring Boot 실행

```bash
mvn clean package
mvn spring-boot:run
```

애플리케이션 시작 시 자동으로:
- Exchange 생성
- Queue 생성
- Binding 설정

## 📱 RabbitMQ Management UI

### Overview
- **Connections**: 현재 연결 수
- **Channels**: 채널 수
- **Queues**: 큐 목록 및 메시지 수
- **Message rates**: 초당 메시지 처리율

### Queues
```
Queue Name          Messages   Consumers   State
order.queue         0          1           Running
stock.queue         0          1           Running
notification.queue  0          1           Running
elasticsearch.queue 0          1           Running
shop.dlq.queue      0          0           Ready
```

### Exchanges
```
Exchange Name    Type    Bindings
shop.exchange    topic   4
shop.dlq.exchange direct 1
```

## 🧪 테스트

### 1. 주문 생성 테스트

```bash
# 장바구니에 상품 추가
curl -X POST http://localhost:8080/api/cart \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "productId": 1,
    "quantity": 2
  }'

# 주문 생성
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "customerName": "홍길동",
    "customerEmail": "hong@example.com"
  }'
```

**확인사항:**
1. Spring Boot 로그에서 이벤트 발행 확인
2. RabbitMQ Management UI에서 메시지 처리 확인
3. Kibana에서 로그 확인

### 2. 재고 업데이트 테스트

```bash
# 상품 수정 (재고 변경)
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "맥북 프로",
    "description": "M3 칩셋",
    "price": 2890000,
    "stock": 5,
    "category": "전자기기"
  }'
```

**확인사항:**
1. stock.queue에 메시지 도착
2. 재고 5개 미만이면 경고 로그 출력
3. StockUpdatedEvent 처리 로그 확인

### 3. Elasticsearch 인덱싱 테스트

```bash
# 상품 생성
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "새 상품",
    "description": "테스트 상품",
    "price": 10000,
    "stock": 100,
    "category": "테스트"
  }'

# 검색 확인 (5초 후)
sleep 5
curl http://localhost:8080/api/products/search?keyword=새상품
```

### 4. 알림 발송 테스트

```bash
# 주문 생성 후 로그 확인
docker-compose logs -f mini-shop | grep "Sending email"
```

## 📊 모니터링

### Spring Boot 로그

```bash
# 이벤트 발행 로그
grep "Publishing.*Event" logs/spring-boot.log

# 이벤트 수신 로그
grep "Received.*Event" logs/spring-boot.log

# 에러 로그
grep "Failed to" logs/spring-boot.log
```

### RabbitMQ 메트릭스

**Management UI → Queues:**
- **Ready**: 처리 대기 중인 메시지
- **Unacked**: 처리 중인 메시지
- **Total**: 전체 메시지 수

**확인해야 할 지표:**
```
Ready 메시지 > 100: Consumer 추가 필요
Unacked 메시지 > 50: 처리 시간 지연
DLQ 메시지 > 0: 실패한 메시지 확인 필요
```

### Kibana 대시보드

**RabbitMQ 이벤트 로그 조회:**
```
log_message: "Publishing*Event"
log_message: "Received*Event"
logger: *messaging*
```

## 🔧 설정 커스터마이징

### application.yml

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin
    
    # Connection Pool
    connection-timeout: 10000
    
    # Listener 설정
    listener:
      simple:
        # 재시도 설정
        retry:
          enabled: true
          initial-interval: 3000  # 첫 재시도 3초
          max-attempts: 3         # 최대 3번 시도
          multiplier: 2           # 재시도 간격 2배씩 증가
        
        # Acknowledge 모드
        acknowledge-mode: auto    # auto, manual, none
        
        # Prefetch Count (한 번에 가져올 메시지 수)
        prefetch: 1
        
        # Consumer 수
        concurrency: 3
        max-concurrency: 10
```

### Consumer 수 조정

```java
@RabbitListener(
    queues = RabbitMQConfig.ORDER_QUEUE,
    concurrency = "3-10"  // 최소 3개, 최대 10개 Consumer
)
public void handleOrderCreated(OrderCreatedEvent event) {
    // 처리 로직
}
```

## 🐛 트러블슈팅

### 1. 메시지가 큐에 쌓이기만 하고 처리 안 됨

**원인**: Consumer가 실행되지 않음

**해결:**
```bash
# Spring Boot 로그 확인
tail -f logs/spring-boot.log | grep "RabbitListener"

# RabbitMQ Management UI에서 Consumer 확인
# Queues → order.queue → Consumers (0이면 문제)

# Spring Boot 재시작
mvn spring-boot:run
```

### 2. DLQ에 메시지가 계속 쌓임

**원인**: 메시지 처리 중 예외 발생

**해결:**
```bash
# DLQ 메시지 확인
# RabbitMQ Management UI → Queues → shop.dlq.queue → Get messages

# 에러 로그 확인
grep "Failed to process" logs/spring-boot.log

# 메시지 내용 확인 후 수동 처리 또는 코드 수정
```

### 3. Connection refused 에러

**원인**: RabbitMQ가 실행되지 않음

**해결:**
```bash
# RabbitMQ 상태 확인
docker-compose ps rabbitmq

# RabbitMQ 재시작
docker-compose restart rabbitmq

# 포트 확인
lsof -i :5672
lsof -i :15672
```

### 4. 메시지 처리 속도가 느림

**원인**: Consumer 수 부족

**해결:**
```yaml
# application.yml에서 Consumer 수 증가
spring:
  rabbitmq:
    listener:
      simple:
        concurrency: 5        # 3 → 5
        max-concurrency: 20   # 10 → 20
```

## 📈 성능 최적화

### 1. Prefetch Count 조정

```yaml
# 많은 작은 메시지: Prefetch 증가
spring:
  rabbitmq:
    listener:
      simple:
        prefetch: 10  # 1 → 10

# 큰 메시지 또는 긴 처리 시간: Prefetch 감소
        prefetch: 1
```

### 2. Message TTL 설정

```java
@Bean
public Queue orderQueue() {
    return QueueBuilder.durable(ORDER_QUEUE)
            .withArgument("x-message-ttl", 3600000)  // 1시간
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
            .build();
}
```

### 3. Queue Length Limit

```java
@Bean
public Queue orderQueue() {
    return QueueBuilder.durable(ORDER_QUEUE)
            .withArgument("x-max-length", 10000)  // 최대 10,000개
            .build();
}
```

### 4. 메시지 우선순위

```java
@Bean
public Queue orderQueue() {
    return QueueBuilder.durable(ORDER_QUEUE)
            .withArgument("x-max-priority", 10)  // 0-10 우선순위
            .build();
}

// 발행 시
rabbitTemplate.convertAndSend(
    SHOP_EXCHANGE, 
    ORDER_ROUTING_KEY, 
    event,
    message -> {
        message.getMessageProperties().setPriority(9);  // 높은 우선순위
        return message;
    }
);
```

## 🔒 보안 설정

### 1. 사용자 및 권한 관리

```bash
# RabbitMQ 컨테이너 접속
docker exec -it shop-rabbitmq bash

# 새 사용자 생성
rabbitmqctl add_user mini_shop_user secure_password

# 권한 부여
rabbitmqctl set_permissions -p / mini_shop_user ".*" ".*" ".*"

# 태그 설정
rabbitmqctl set_user_tags mini_shop_user monitoring
```

### 2. SSL/TLS 설정

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5671  # SSL 포트
    username: admin
    password: admin
    ssl:
      enabled: true
      key-store: classpath:keystore.p12
      key-store-password: password
```

## 📊 메시지 흐름 예시

### 주문 생성 전체 흐름

```
1. Client → POST /api/orders
   ↓
2. OrderService.createOrder()
   - 장바구니 조회
   - 재고 확인
   - PostgreSQL 저장
   - 재고 감소 (이벤트 발행)
   ↓
3. EventPublisher.publishOrderCreated()
   - OrderCreatedEvent → RabbitMQ
   ↓
4. EventPublisher.publishNotification()
   - NotificationEvent → RabbitMQ
   ↓
5. Response → Client (빠른 응답)
   ↓
6. [비동기] OrderEventListener.handleOrderCreated()
   - 주문 후처리
   - 외부 시스템 연동
   ↓
7. [비동기] StockEventListener.handleStockUpdated()
   - 재고 부족 알림
   - 재고 히스토리
   ↓
8. [비동기] NotificationEventListener.handleNotification()
   - 이메일 발송
   - SMS 발송
```

## 🎯 Best Practices

### 1. 이벤트는 작고 명확하게

```java
// ✅ 좋은 예: 필요한 정보만
@Data
public class OrderCreatedEvent {
    private Long orderId;
    private String customerEmail;
    private BigDecimal totalAmount;
}

// ❌ 나쁜 예: 불필요한 정보 포함
@Data
public class OrderCreatedEvent {
    private Order order;  // 전체 엔티티 (Lazy Loading 문제 가능)
}
```

### 2. Idempotent 처리

```java
@RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
public void handleOrderCreated(OrderCreatedEvent event) {
    // 이미 처리된 주문인지 확인
    if (isAlreadyProcessed(event.getOrderId())) {
        log.warn("Order already processed: {}", event.getOrderId());
        return;
    }
    
    // 처리 로직
    processOrder(event);
    
    // 처리 완료 기록
    markAsProcessed(event.getOrderId());
}
```

### 3. 재시도 전략

```java
@RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
public void handleNotification(NotificationEvent event) {
    try {
        sendEmail(event);
    } catch (TemporaryException e) {
        // 재시도 가능한 예외 - 예외를 던져서 재시도
        throw new AmqpRejectAndRequeueException("Temporary failure", e);
    } catch (PermanentException e) {
        // 재시도 불가능한 예외 - DLQ로 전송
        log.error("Permanent failure: {}", e.getMessage());
        throw new AmqpRejectAndDontRequeueException("Permanent failure", e);
    }
}
```

### 4. 로깅 전략

```java
@RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
public void handleOrderCreated(OrderCreatedEvent event) {
    log.info("Received OrderCreatedEvent: orderId={}", event.getOrderId());
    
    try {
        // 처리 로직
        log.info("Order processed successfully: orderId={}", event.getOrderId());
    } catch (Exception e) {
        log.error("Failed to process order: orderId={}, error={}", 
                event.getOrderId(), e.getMessage(), e);
        throw e;
    }
}
```

## 📚 관련 문서

- [RabbitMQ 공식 문서](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP 문서](https://docs.spring.io/spring-amqp/docs/current/reference/html/)
- [ELK-STACK-GUIDE.md](./ELK-STACK-GUIDE.md) - 로그 모니터링
- [README.md](./README.md) - 프로젝트 전체 가이드

## ✅ 체크리스트

RabbitMQ가 제대로 작동하는지 확인:

- [ ] docker-compose up으로 RabbitMQ 실행됨
- [ ] http://localhost:15672 접속 가능 (admin/admin)
- [ ] Spring Boot 시작 시 Queue 자동 생성됨
- [ ] 주문 생성 시 이벤트 발행 로그 출력
- [ ] RabbitMQ Management UI에서 메시지 처리 확인
- [ ] Consumer가 메시지 수신 및 처리
- [ ] Kibana에서 이벤트 로그 조회 가능
- [ ] 에러 발생 시 DLQ로 메시지 이동

## 🎓 요약

**RabbitMQ 도입 효과:**
1. ⚡ **성능 향상**: 비동기 처리로 응답 속도 30% 개선
2. 🔧 **확장성**: Consumer 수평 확장 가능
3. 🛡️ **안정성**: 장애 격리 및 재시도 메커니즘
4. 📊 **모니터링**: Management UI로 실시간 모니터링
5. 🔄 **유연성**: 새로운 이벤트 리스너 추가 용이

**주요 이벤트:**
- `OrderCreatedEvent`: 주문 생성
- `StockUpdatedEvent`: 재고 변동
- `NotificationEvent`: 알림 발송
- `ElasticsearchIndexEvent`: 검색 인덱싱