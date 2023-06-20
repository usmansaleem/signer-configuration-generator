# syntax=docker/dockerfile:1

# Create a custom Java runtime
FROM eclipse-temurin:17 as jre-build
RUN JAVA_TOOL_OPTIONS="-Djdk.lang.Process.launchMechanism=vfork" $JAVA_HOME/bin/jlink \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# Create our application distribution without running tests
FROM ubuntu:latest as app-build
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME
COPY . /opt/app/
WORKDIR /opt/app
RUN ./gradlew distTar -x test --no-daemon

# Bundle our application using jre and dist from prevous multistage builds
FROM ubuntu:latest
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME
COPY --from=app-build /opt/app/build/distributions/signer-configuration-generator.tar.gz /opt/
RUN tar -xvzf /opt/signer-configuration-generator.tar.gz -C /opt/ && rm /opt/signer-configuration-generator.tar.gz
WORKDIR /opt/signer-configuration-generator

ENTRYPOINT ["/opt/signer-configuration-generator/bin/signer-configuration-generator"]