FROM registry.bizsaas.net/arm64v8/java:openjdk8-alpine-bash-cn-dockerize

MAINTAINER cal


EXPOSE 8080
COPY ./dubbo-admin-server*.jar /root.jar
COPY Dockerfile /Dockerfile
CMD java -jar $JAVA_OPTS /root.jar