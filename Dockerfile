#FROM openjdk:8-jdk-alpine
FROM maven:3.5-jdk-8-alpine as builder
WORKDIR /app

# 删除之前的镜像文件
RUN rm -rf /app/xsoj-code-sandbox*

#ADD target/xsoj-code-sandbox-0.0.1-SNAPSHOT-jar-with-dependencies.jar xsoj-code-sandbox.jar
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests
EXPOSE 8090

#ENTRYPOINT ["java","-jar"]
#
#CMD ["xsoj-code-sandbox.jar"]
CMD ["java","-jar","/app/target/xsoj-code-sandbox-0.0.1-SNAPSHOT.jar"]