FROM registry.bizsaas.net/java:openjdk8-alpine-bash-cn-dockerize

#MAINTAINER cal


EXPOSE 8080
COPY ./dubbo-admin-server*.jar /root/root.jar
COPY Dockerfile /Dockerfile
WORKDIR /root
CMD java -jar $JAVA_OPTS /root/root.jar $SPRING_ARGS