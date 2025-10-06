# ---- Build stage ----
FROM gradle:8.9-jdk17-alpine AS build
WORKDIR /workspace

# 커밋마다 COPY 레이어를 강제로 갱신하기 위한 캐시 버스터
ARG GIT_SHA=dev
LABEL org.opencontainers.image.revision=$GIT_SHA

# 1) Gradle 래퍼/설정 먼저 복사 → 의존성 레이어 캐시
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle* build.gradle* ./
RUN chmod +x ./gradlew

# (선택) 의존성 프리워밍 — 이후 빌드 속도 향상
RUN ./gradlew --no-daemon -q help || true

# 2) 실제 앱 소스 복사 (이 시점부터 커밋 변화가 캐시 무효화 트리거)
COPY . .

# (선택) 디버깅용: 컨테이너 안 소스에 오래된 심볼이 남았는지 검사
# RUN grep -Rin "fromEntity" src/main/java || echo "OK: no 'fromEntity'"

# 3) 애플리케이션 빌드
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드 산출물 복사
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
