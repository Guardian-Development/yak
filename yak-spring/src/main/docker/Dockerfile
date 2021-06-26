####
# This Dockerfile is used to package the yak-spring-server application
#
# Build the image with:
#
# ./gradlew buildDocker
#
###

# build custom JRE
FROM openjdk:17-alpine AS jre-build
WORKDIR /app

COPY build/lib/* build/lib/
COPY build/libs/yak-spring-1.0.0.SNAPSHOT.jar build/app.jar

RUN jlink --verbose \
--compress 2 \
--strip-java-debug-attributes \
--no-header-files \
--no-man-pages \
--output jre \
--add-modules java.base\
,java.logging\
,java.xml\
,jdk.unsupported\
,java.sql\
,java.naming\
,java.desktop\
,java.management\
,java.security.jgss\
,java.instrument\
# needed for jfr and jcmd
,jdk.jfr,jdk.management.jfr,jdk.jcmd

# take a smaller runtime image for the final output
FROM alpine:latest
WORKDIR /deployment

COPY --from=jre-build /app/jre /jre
COPY --from=jre-build /app/build/lib/* lib/
COPY --from=jre-build /app/build/app.jar app.jar

COPY src/main/docker/resources/log4j2.xml /opt/spring/config/log4j2.xml
COPY src/main/docker/resources/application.yml /opt/spring/config/application.yml
COPY src/main/docker/resources/jfr/default.jfc /opt/spring/metrics/default.jfc
COPY src/main/docker/resources/jfr/profile.jfc /opt/spring/metrics/profile.jfc

RUN mkdir /opt/spring/metrics/output

ENV SPRING_JFR_PROFILE_PATH=/opt/spring/metrics/default.jfc
ENV SPRING_JFR_OUTPUT_PATH=/opt/spring/metrics/output/yak_spring_jfr_recording
ENV SPRING_HEAPDUMP_PATH=/opt/spring/metrics/output/yak-spring-server-heap-dump
ENV SPRING_LOG4J_CONFIG_PATH=/opt/spring/config/log4j2.xml
ENV SPRING_APPLICATION_CONFIG_PATH=/opt/spring/config/application.yml

ENTRYPOINT /jre/bin/java \
    -XX:StartFlightRecording=settings=${SPRING_JFR_PROFILE_PATH},filename=${SPRING_JFR_OUTPUT_PATH}-$(date +%F-%T).jfr,dumponexit=true,disk=true,maxsize=2048m,maxage=6h,delay=30s \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=${SPRING_HEAPDUMP_PATH}-$(date +%F-%T).bin \
    -Dlog4j.configurationFile=${SPRING_LOG4J_CONFIG_PATH} \
    -jar app.jar --spring.config.location=${SPRING_APPLICATION_CONFIG_PATH}