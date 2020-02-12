FROM adorsys/java:8

WORKDIR /opt/
COPY ./target/example-starter-executable.jar .

EXPOSE 8888

CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/example-starter-executable.jar
