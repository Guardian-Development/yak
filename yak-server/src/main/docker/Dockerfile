####
# This Dockerfile is used to package the yak-server application
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
COPY build/libs/yak-server-1.0.0.SNAPSHOT.jar build/app.jar

# find JDK dependencies dynamically from jar
RUN jdeps \
# dont worry about missing modules
--ignore-missing-deps \
# suppress any warnings printed to console
-q \
# java release version targeting
--multi-release 17 \
# output the dependencies at end of run
--print-module-deps \
# specify the the dependencies for the jar
--class-path build/lib/* \
# pipe the result of running jdeps on the app jar to file
build/app.jar > jre-deps.info

RUN jlink --verbose \
--compress 2 \
--strip-java-debug-attributes \
--no-header-files \
--no-man-pages \
--output jre \
--add-modules $(cat jre-deps.info)\
# needed for jfr and jcmd
,jdk.jfr,jdk.management.jfr,jdk.jcmd

# take a smaller runtime image for the final output
FROM alpine:latest
WORKDIR /deployment

COPY --from=jre-build /app/jre /jre
COPY --from=jre-build /app/build/lib/* lib/
COPY --from=jre-build /app/build/app.jar app.jar

COPY src/main/docker/resources/log4j2.xml /opt/yak/config/log4j2.xml
COPY src/main/docker/resources/example-yak-config.json /opt/yak/config/yak-config.json
COPY src/main/docker/resources/jfr/default.jfc /opt/yak/metrics/default.jfc
COPY src/main/docker/resources/jfr/profile.jfc /opt/yak/metrics/profile.jfc

RUN mkdir /opt/yak/metrics/output

ENV YAK_JFR_PROFILE_PATH=/opt/yak/metrics/default.jfc
ENV YAK_JFR_OUTPUT_PATH=/opt/yak/metrics/output/yak_jfr_recording
ENV YAK_HEAPDUMP_PATH=/opt/yak/metrics/output/yak-server-heap-dump
ENV YAK_CONFIG_FILE_PATH=/opt/yak/config/yak-config.json
ENV YAK_LOG4J_CONFIG_PATH=/opt/yak/config/log4j2.xml

ENTRYPOINT /jre/bin/java \
    -XX:StartFlightRecording=settings=${YAK_JFR_PROFILE_PATH},filename=${YAK_JFR_OUTPUT_PATH}-$(date +%F-%T).jfr,dumponexit=true,disk=true,maxsize=2048m,maxage=6h,delay=30s \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=${YAK_HEAPDUMP_PATH}-$(date +%F-%T).bin \
    -Dlog4j.configurationFile=${YAK_LOG4J_CONFIG_PATH} \
    -jar app.jar \
    ${YAK_CONFIG_FILE_PATH}