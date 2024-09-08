cat Dockerfile
FROM maven:3.9.9-amazoncorretto-17

USER root
COPY . /load-balancer
RUN ["rm" ,"-rf", "/load-balancer/target"]
WORKDIR /load-balancer

ENTRYPOINT ["sh","entrypoint.sh"]
