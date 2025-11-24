FROM registry.bizsaas.net/java:openjdk17-alpine-bash-cn-skywalking

#MAINTAINER cal


EXPOSE 8080
COPY ./dubbo-admin-server*.jar /root/root.jar
COPY Dockerfile /Dockerfile
WORKDIR /root
ENV GC_LOG_DIR=/root/logs/dubbo-admin
RUN mkdir -p /root/logs/dubbo-admin
#CMD source /jvm.sh ; java -jar "$JAVA_OPTS" /root/root.jar "$SPRING_ARGS"
CMD source /jvm.sh ; java -jar $JAVA_OPTS /root/root.jar $SPRING_ARGS
