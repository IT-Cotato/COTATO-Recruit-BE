# Java 17 런타임 환경을 기반 이미지로 사용
FROM eclipse-temurin:17-jre-jammy

# 필수 패키지 설치 (헬스체크를 위한 curl)
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일의 경로를 변수로 정의
ARG JAR_FILE=build/libs/*.jar

# 호스트의 JAR 파일을 컨테이너로 복사
COPY ${JAR_FILE} app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 타임존 설정
ENV TZ=Asia/Seoul

# 컨테이너 실행 시 Java 애플리케이션 시작
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]