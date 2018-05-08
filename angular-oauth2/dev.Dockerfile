FROM adorsys/angular-cli:v1.7.4

COPY ./docker/run-dev.cmd.bash /opt/run-dev.cmd.bash

RUN mkdir -p /opt/src
WORKDIR /opt/src

EXPOSE 4200

CMD ["/bin/sh", "/opt/run-dev.cmd.bash"]
