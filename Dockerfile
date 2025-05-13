FROM openjdk:17

WORKDIR /app

COPY target/coreprocessor-0.0.1-SNAPSHOT.jar app.jar
COPY postpack.xml postpack.xml
COPY nibss.xml nibss.xml
COPY nibss.ks nibss.ks
ADD deploy deploy

EXPOSE 5335 5335
EXPOSE 5339 5339
EXPOSE 8001 8001
ENTRYPOINT ["java","-jar","app.jar"]