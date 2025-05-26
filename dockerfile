# ─────────────────────────────────────────────────────────────
# Stage 1: Build with Gradle Wrapper + JDK21
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# 1. Copy only wrapper & build scripts to leverage cache
COPY gradlew .
COPY gradle gradle
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle

# 2. Make wrapper executable & warm up dependencies
RUN chmod +x gradlew \
 && ./gradlew --no-daemon help > /dev/null

# 3. Copy sources and produce the tar distribution
COPY . .
RUN ./gradlew clean distTar --no-daemon


# ─────────────────────────────────────────────────────────────
# Stage 2: jlink a custom JRE
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21 AS jre-build

# create a minimal, compressed JRE at /javaruntime
RUN JAVA_TOOL_OPTIONS="-Djdk.lang.Process.launchMechanism=vfork" \
    $JAVA_HOME/bin/jlink \
      --add-modules ALL-MODULE-PATH \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=zip-6 \
      --output /javaruntime


# ─────────────────────────────────────────────────────────────
# Stage 3: Final image on Ubuntu:latest
# ─────────────────────────────────────────────────────────────
FROM ubuntu:latest
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# 1. Bring in the linked JRE
COPY --from=jre-build /javaruntime $JAVA_HOME

# 2. Copy & unpack your distribution tar
WORKDIR /app
COPY --from=builder \
     /workspace/build/distributions/signer-configuration-generator-*.tar.gz \
     /tmp/signer.tar.gz

RUN mkdir -p /app \
 && tar --strip-components=1 -xzf /tmp/signer.tar.gz -C /app \
 && rm /tmp/signer.ta.gzr \
 && chmod +x /app/bin/signer-configuration-generator

# 3. Switch to a non-root user (optional, but recommended)
RUN useradd --system --create-home appuser \
 && chown -R appuser:appuser /app
USER appuser

ENTRYPOINT ["bin/signer-configuration-generator"]
