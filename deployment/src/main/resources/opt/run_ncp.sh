#!/bin/sh
CONTAINER_FIRST_STARTUP="CONTAINER_FIRST_STARTUP"
if [ ! -e /$CONTAINER_FIRST_STARTUP ] &&  [ -d "/opt/openncp-configuration-utility" ]; then
    touch /$CONTAINER_FIRST_STARTUP
    cd /opt/openncp-configuration-utility 
    java -jar openncp-configuration-utility.jar
fi

export CATALINA_OPTS="-Dserver.ehealth.mode=${OPENNCP_SERVER_EHEALTH_MODE:-PPT}  ${OPENNCP_JAVA_VM_PROPERTIES:-} -DopenATNA.properties.path=file:$EPSOS_PROPS_PATH/ATNA_resources/openatna.properties -Dorg.apache.tomcat.util.digester.PROPERTY_SOURCE=at.gv.bmg.openncp.tomcat.ConfigurationPropertySource"

/usr/local/tomcat/bin/catalina.sh jpda run
