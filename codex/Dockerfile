#
# Build stage
#
FROM maven:3.8.6-amazoncorretto-18 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml -Dmaven.test.skip=true clean package

#
# Package stage
#
FROM maven:3.8.6-amazoncorretto-18
COPY --from=build /home/app/target/codex-0.0.1-SNAPSHOT.jar /usr/local/lib/codex.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/usr/local/lib/codex.jar"]
