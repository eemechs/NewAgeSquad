FROM openjdk:17.0.1-jdk-slim@sha256:9a37f2c649301b955c2fd31fb180070404689cacba0f77404dd20afb1d7b8d84 AS build
WORKDIR /src
COPY App.java HttpServerApp.java ./
RUN javac -d /opt/classes App.java HttpServerApp.java

FROM openjdk:17.0.1-jdk-slim@sha256:9a37f2c649301b955c2fd31fb180070404689cacba0f77404dd20afb1d7b8d84
WORKDIR /app
COPY --from=build /opt/classes /opt/classes
CMD ["java", "-cp", "/opt/classes", "HttpServerApp"]
