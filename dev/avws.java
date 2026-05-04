///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//REPOS central
//REPOS umleditor=https://jars.umleditor.org/
//REPOS interlis=https://jars.interlis.org/
//DEPS ch.ehi.avwebservice:av-web-service:1.0.0-SNAPSHOT:plain
//DEPS org.springframework.boot:spring-boot-starter-web:3.5.9
//DEPS org.springframework.boot:spring-boot-starter-actuator:3.5.9
//DEPS org.springframework.boot:spring-boot-starter-jdbc:3.5.9
//DEPS org.springframework:spring-oxm:6.2.15
//DEPS org.postgresql:postgresql:42.6.2
//DEPS ch.ehi:ehibasics:1.4.1
//DEPS com.vividsolutions:jts-core:1.14.0
//DEPS org.glassfish.jaxb:jaxb-core:4.0.5
//DEPS org.glassfish.jaxb:jaxb-runtime:4.0.5

import org.springframework.boot.SpringApplication;

public class avws {
    public static void main(String[] args) throws Exception {
        String mainClass = System.getProperty(
            "mainClass",
            "ch.ehi.av.webservice.Application"
        );

        SpringApplication.run(Class.forName(mainClass), args);
    }
}