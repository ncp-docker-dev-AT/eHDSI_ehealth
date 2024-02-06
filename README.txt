eHealth Project
===============
It has been created from OpenNCP projects and restructured to a multi module project. In order to build it just do the following:

Main Modules
------------
assertion-validator
audit-manager
cda-display-tool
configuration-manager
consent-manager
data-model
default-policy-manager
e-sens-non-repudiation
eadc
openatna
openncp-common-components
openncp-gateway
openncp-gazelle-validation
openstork
protocol-terminators
security-manager
translations-and-mappings
translations-and-mappings-ws
trc-sts
trc-sts-client
tsam
tsam-sync
util

Maven Configuration
-------------------
export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=350m"
mvn clean package install -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

The ssl.insecure flags are for allowing eHDSI Gazelle endpoints needed for parsing ws endpoints

Please note that in order to use the IHE Nexus repository, you have to import the repository certificate into your
Maven configuration or into your JAVA_HOME/jre/lib/security/cacerts keystore (this certificate is updated frequently)
keytool -import -alias gazelle.ehdsi.eu -file gazelle.ehdsi.eu.crt -keystore JAVA_HOME/jre/lib/security/cacerts

Tips for creating new versions
------------------------------
From the root directory just run the following command mvn versions:set -DnewVersion=<new version>


Dependency check
----------------
mvn dependency-check:aggregate -DskipTests -U -T2C
or
mvn clean verify -DskipTests -T2C
or
mvn clean dependency-check:aggregate -DskipTests -U -T2C
then see the file from target/dependency-check-report.html