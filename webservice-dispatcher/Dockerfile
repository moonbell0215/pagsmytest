FROM adoptopenjdk/openjdk11:ubi
VOLUME /tmp
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
EXPOSE 8081
ENV APPLICATIONINSIGHTS_ROLE_NAME=webservice-dispatcher
RUN curl -L https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.0.0/applicationinsights-agent-3.0.0.jar -o /app/applicationinsights-agent-3.0.0.jar
ENTRYPOINT ["sh", "-c", "java -javaagent:/app/applicationinsights-agent-3.0.0.jar -XX:MaxRAMPercentage=75 ${JAVA_OPTS} -cp app:app/lib/* com.dht.pags.webservice.dispatcher.WebserviceDispatcherApplication --spring.profiles.active=${SPRING_PROFILES}"]