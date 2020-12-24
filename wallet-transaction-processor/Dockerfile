FROM adoptopenjdk/openjdk11:ubi
VOLUME /tmp
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75 ${JAVA_OPTS} -cp app:app/lib/* com.dht.pags.wallet.transactionprocessor.TransactionProcessorApplication --spring.profiles.active=${SPRING_PROFILES}"]