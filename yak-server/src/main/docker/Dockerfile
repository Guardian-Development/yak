####
# This Dockerfile is used to package the yak-server application
#
# Build the image with:
#
# ./gradlew buildDocker
#
# Then run the container using:
#
# ./gradlew runDocker
#
###

# build custom JRE
FROM openjdk:14-alpine AS jre-build
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
--multi-release 14 \
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
# needed for jfr
,jdk.jfr,jdk.management.jfr

# take a smaller runtime image for the final output
FROM alpine:latest
WORKDIR /deployment

COPY --from=jre-build /app/jre jre
COPY --from=jre-build /app/build/lib/* lib/
COPY --from=jre-build /app/build/app.jar app.jar
RUN mkdir jfr

ENV CONFIG_FILE_PATH=/opt/yak/config/yak-config.json

ENTRYPOINT jre/bin/java -jar app.jar ${CONFIG_FILE_PATH}