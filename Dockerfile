FROM openjdk:8-jre-alpine
MAINTAINER Andrei
ENV APP_DIR /opt/lib
COPY ./*.jar ${APP_DIR}/geozone.jar
EXPOSE 8860
RUN mkdir ${APP_DIR}/logs
WORKDIR ${APP_DIR}
VOLUME ${APP_DIR}/logs
ENTRYPOINT ["java", "-Xmx500m", "-jar", "geozone.jar"]
