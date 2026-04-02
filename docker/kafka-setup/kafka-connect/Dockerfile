FROM confluentinc/cp-kafka-connect:7.6.1

USER root

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.7.6
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:latest

ADD https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar /usr/share/confluent-hub-components/confluentinc-kafka-connect-jdbc/lib/sqlite-jdbc-3.45.1.0.jar

USER appuser
