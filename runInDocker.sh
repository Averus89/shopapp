!#/bin/bash

./gradlew clean bootJar
docker-compose -f docker-compose.yaml up --build