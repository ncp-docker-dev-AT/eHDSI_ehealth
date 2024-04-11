docker exec -it -w /opt/openncp-tsam-sync `docker ps -aqf "name=^openncp-officer$"`  java -Dtsam-sync.cts.url=https://webgate.training.ec.europa.eu/ehealth-term-server -Dtsam-sync.cts.username= -Dtsam-sync.cts.password= -Dtsam-sync.datasource.driver=org.mariadb.jdbc.Driver -jar openncp-tsam-sync.jar