FROM tomcat:9.0.84-jdk17-temurin-jammy
ARG CONTEXT_DIRECTORY
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
EXPOSE 5005
EXPOSE 8080
EXPOSE 8444
ENV EPSOS_PROPS_PATH=/opt/openncp-configuration/
ENV OPENNCP_PROPERTIES_JDBC_URL=jdbc:mariadb://openncp-db:3306/ehealth_properties?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8&useSSL=false
ENV OPENNCP_PROPERTIES_JDBC_USER=openncp
ENV OPENNCP_PROPERTIES_JDBC_PASSWORD=openncp1234
CMD [ "/opt/run_ncp.sh" ]