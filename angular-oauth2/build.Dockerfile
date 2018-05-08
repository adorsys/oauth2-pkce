FROM adorsys/angular-cli:v1.7.4

COPY ./docker/build.cmd.bash /opt/build.cmd.bash

RUN mkdir -p /opt/src
WORKDIR /opt/src

CMD ["/bin/sh", "/opt/build.cmd.bash"]
