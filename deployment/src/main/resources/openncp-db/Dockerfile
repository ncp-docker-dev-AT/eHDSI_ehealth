FROM mariadb:lts-jammy
ARG INIT_SQL_FILE
COPY $INIT_SQL_FILE /docker-entrypoint-initdb.d/
RUN chown mysql:mysql /docker-entrypoint-initdb.d/*.sql