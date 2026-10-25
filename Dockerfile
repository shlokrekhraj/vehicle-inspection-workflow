# Vehicle Inspection Workflow - runtime image
# The jar is built once by Maven (locally or in Jenkins) and packaged here,
# so the image contains exactly the artefact that passed the tests.
#
#   mvn -B package -DskipTests
#   docker build -t vehicle-inspection-workflow:1.2.0 --build-arg APP_VERSION=1.2.0 .

FROM eclipse-temurin:17-jre-alpine

ARG APP_VERSION=dev
ARG GIT_COMMIT=unknown

LABEL org.opencontainers.image.title="vehicle-inspection-workflow" \
      org.opencontainers.image.description="Containerized Vehicle Inspection Workflow" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}" \
      org.opencontainers.image.authors="Shlok Rekhraj"

# Run as an unprivileged user
RUN addgroup -S vehicleapp && adduser -S -G vehicleapp vehicleapp

WORKDIR /app
COPY --chown=vehicleapp:vehicleapp target/vehicle-inspection-workflow-*.jar app.jar

ENV APP_PORT=8080 \
    APP_VERSION=${APP_VERSION} \
    APP_ENV=docker \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

USER vehicleapp
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- "http://127.0.0.1:${APP_PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
