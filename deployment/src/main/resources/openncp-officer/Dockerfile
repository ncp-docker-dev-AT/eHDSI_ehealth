FROM openncp/openncp-tomcat-base:latest
ARG CONTEXT_DIRECTORY
COPY $CONTEXT_DIRECTORY/openatna-web /usr/local/tomcat/webapps/openatna-web
COPY $CONTEXT_DIRECTORY/translations-and-mappings-ws /usr/local/tomcat/webapps/translations-and-mappings-ws
COPY $CONTEXT_DIRECTORY/openncp-gateway /usr/local/tomcat/webapps/openncp-gateway
COPY $CONTEXT_DIRECTORY/openncp-gateway-backend /usr/local/tomcat/webapps/openncp-gateway-backend
COPY $CONTEXT_DIRECTORY/TRC-STS /usr/local/tomcat/webapps/TRC-STS
COPY $CONTEXT_DIRECTORY/ehealth-portal /usr/local/tomcat/webapps/ehealth-portal
COPY $CONTEXT_DIRECTORY/ehealth-portal-backend /usr/local/tomcat/webapps/ehealth-portal-backend
ADD $CONTEXT_DIRECTORY/openncp-configuration-utility/ /opt/openncp-configuration-utility
ADD $CONTEXT_DIRECTORY/openncp-tsam-exporter/ /opt/openncp-tsam-exporter
ADD $CONTEXT_DIRECTORY/openncp-tsam-sync/ /opt/openncp-tsam-sync
ADD $CONTEXT_DIRECTORY/openncp-configuration/ /opt/openncp-configuration-template
