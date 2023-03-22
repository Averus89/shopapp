FROM eclipse-temurin:17.0.6_10-jre-alpine

RUN addgroup -S shopapp && adduser -S shopapp -G shopapp

ENV SHOPAPP_PATH=/opt/shopapp

RUN mkdir ${SHOPAPP_PATH}
WORKDIR ${SHOPAPP_PATH}
COPY build/libs/*.jar ${SHOPAPP_PATH}/app.jar

RUN chown -R shopapp:shopapp ${SHOPAPP_PATH}

USER shopapp

ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar

CMD java ${JAVA_OPTS} -jar ${SHOPAPP_PATH}/app.jar ${SHOPAPP_ARGS}

EXPOSE 8080
