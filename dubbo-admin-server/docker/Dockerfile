FROM registry.bizsaas.net/tongweb:7.0.4.7-vmoptions

MAINTAINER cal


EXPOSE 8080
COPY ./dubbo-admin-server*.jar /root.jar
COPY Dockerfile /Dockerfile
CMD java -jar $JAVA_OPTS /root.jar $SPRING_ARGS