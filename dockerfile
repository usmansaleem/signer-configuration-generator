# syntax=docker/dockerfile:1

# Stage 1: jlink a custom JRE
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

# Stage 2: Final image
FROM ubuntu:22.04
ARG TAR_FILE
RUN test -n "$TAR_FILE" || { echo "TAR_FILE build argument is required"; exit 1; }

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# 1. Copy JRE
COPY --from=jre-build /javaruntime $JAVA_HOME

# 2. Install distribution
COPY ${TAR_FILE} /tmp/signer.tar.gz
RUN mkdir -p /app \
 && tar --strip-components=1 -xzf /tmp/signer.tar.gz -C /app \
 && rm /tmp/signer.tar.gz \
 && chmod +x /app/bin/signer-configuration-generator

# 3. Non-root setup
RUN useradd --system --create-home --uid 1001 --no-log-init appuser \
 && chown -R appuser:appuser /app

USER appuser
WORKDIR /app

ENTRYPOINT ["bin/signer-configuration-generator"]
