FROM maven:3.8.6-openjdk-18

COPY . .

RUN mvn -Dmaven.test.skip=true clean package -X
