FROM registry.bizsaas.net/java:openjdk8-alpine-bash-cn-dockerize

MAINTAINER cal


EXPOSE 8080
COPY ./dubbo-admin-server*.jar /root.jar
COPY Dockerfile /Dockerfile
CMD java -jar $JAVA_OPTS /root.jar $SPRING_ARGS