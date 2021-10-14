FROM amazoncorretto:11-alpine-jdk
VOLUME /tmp
COPY target/backend-file-uploader-api-0.0.1-SNAPSHOT.jar FileUploader-service.jar
ENTRYPOINT ["java", "-jar", "UsersService.jar"]
