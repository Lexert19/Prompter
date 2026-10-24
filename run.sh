sh gradlew build --continuous
docker exec mailserver setup email add ${NOREPLY_EMAIL} ${NOREPLY_PASSWORD}