#!/bin/bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
