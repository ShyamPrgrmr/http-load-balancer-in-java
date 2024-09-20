# Multithreaded HTTP Load Balancer Developed in JAVA. 

Read a blog post for implementation details on [Blog Link](https://medium.com/@shampradhanmarket/developing-multithreaded-load-balancer-in-java-aea1f6d236cc)

## How to run ? 

### Prerequesites
1. Linux machine with bash shell.
2. Docker installed on linux machine. 

### Steps to run locally
1. Clone this repository.
2. Make necessary changes in "http-load-balancer-in-java/src/main/resources/application.properties" like upstream addresses, thread pool size etc. 
3. Run 'bash runbuild.sh -v 20 -r "dockerhub repo name" -p "dockerhub password" -e "environment name" -u "dockerhub username"'

### Steps to run and deploy using Jenkins

--in porgress--
