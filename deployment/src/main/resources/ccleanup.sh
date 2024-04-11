#!/bin/sh
docker ps -a | grep "mariadb" | awk '{ print $1 }' | xargs docker rm -f;docker volume rm openncp_openncp-db-volume || true
docker ps -a | grep "admin" | awk '{ print $1 }' | xargs docker rm -f || true
docker ps -a | grep "openncp" | awk '{ print $1 }' | xargs docker rm -f || true
docker images| grep "openncp/openncp" | awk '{ print $1":"$2 }' | xargs docker image rm
rm -rf ./logs
cd ..
