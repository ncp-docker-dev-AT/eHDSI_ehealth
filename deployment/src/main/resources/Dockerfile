FROM tomcat:9.0.79-jdk17-temurin-jammy as tomcat
ARG CONTEXT_DIRECTORY


ADD $CONTEXT_DIRECTORY/openncp-configuration-utility/ /opt/openncp-configuration-utility
ADD $CONTEXT_DIRECTORY/openncp-tsam-exporter/ /opt/openncp-tsam-exporter
ADD $CONTEXT_DIRECTORY/openncp-tsam-sync/ /opt/openncp-tsam-sync
COPY $CONTEXT_DIRECTORY/opt/run_ncp.sh /opt/run_ncp.sh
COPY $CONTEXT_DIRECTORY/java/conf/security/java.security /usr/local/openjdk-17/conf/security/java.security
COPY $CONTEXT_DIRECTORY/tomcat/conf/server.xml /usr/local/tomcat/conf/server.xml
COPY $CONTEXT_DIRECTORY/tomcat/conf/context.xml /usr/local/tomcat/conf/context.xml
COPY $CONTEXT_DIRECTORY/tomcat/conf/catalina.properties /usr/local/tomcat/conf/catalina.properties
COPY $CONTEXT_DIRECTORY/tomcat/lib /usr/local/tomcat/lib/
COPY $CONTEXT_DIRECTORY/tomcat/bin/catalina.sh /usr/local/tomcat/bin/catalina.sh

RUN chmod +x /usr/local/tomcat/bin/catalina.sh && \
    rm -rf /etc/localtime && \
    ln -s /usr/share/zoneinfo/Europe/Vienna /etc/localtime && \
    chmod +x /opt/run_ncp.sh

FROM tomcat 

# ENV OPEN_NCP_ATNA_HOME=$CONTEXT_DIRECTORY/artifacts/openatna-all.jar
COPY $CONTEXT_DIRECTORY/artifacts/openatna-web.war /usr/local/tomcat/webapps/openatna-web.war
COPY $CONTEXT_DIRECTORY/artifacts/openncp-client-connector.war /usr/local/tomcat/webapps/openncp-client-connector.war
COPY $CONTEXT_DIRECTORY/artifacts/openncp-gateway /usr/local/tomcat/webapps/openncp-gateway
COPY $CONTEXT_DIRECTORY/artifacts/openncp-gateway-backend.war /usr/local/tomcat/webapps/openncp-gateway-backend.war
COPY $CONTEXT_DIRECTORY/artifacts/ehealth-portal /usr/local/tomcat/webapps/ehealth-portal
COPY $CONTEXT_DIRECTORY/artifacts/ehealth-portal-backend.war /usr/local/tomcat/webapps/ehealth-portal-backend.war
COPY $CONTEXT_DIRECTORY/artifacts/openncp-trc-sts.war /usr/local/tomcat/webapps/TRC-STS.war
COPY $CONTEXT_DIRECTORY/artifacts/openncp-ws-server.war /usr/local/tomcat/webapps/openncp-ws-server.war

ENV EPSOS_PROPS_PATH=/opt/openncp-configuration/

EXPOSE 5005
EXPOSE 8443
EXPOSE 8444

CMD [ "/opt/run_ncp.sh" ]