FROM openjdk:12-ea-25-jdk-alpine3.8

COPY target/o12stack-test-center-*.jar /app.jar
COPY elastic-apm-agent-1.10.0.jar /elastic-apm-agent-1.10.0.jar

CMD java -javaagent:/elastic-apm-agent-1.10.0.jar -Delastic.apm.service_name=o12stack-test-center -Delastic.apm.server_urls=$ELASTIC_APM_SERVER_URL -Delastic.apm.secret_token=$ELASTIC_APM_SECRET_TOKEN -Delastic.apm.application_packages=org.o12stack.o12stack.testcenter -jar /app.jar
