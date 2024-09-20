# Multithreaded HTTP Load Balancer Developed in JAVA

Read a blog post with implementation details on [this blog](https://medium.com/@shampradhanmarket/developing-multithreaded-load-balancer-in-java-aea1f6d236cc).

## How to Run?

### Prerequisites
1. A Linux machine with a Bash shell.
2. Docker installed on the Linux machine.

### Steps to Run Locally
1. Clone this repository.
2. Make necessary changes in `http-load-balancer-in-java/src/main/resources/application.properties`, such as upstream addresses, thread pool size, etc.
3. Open `runbuild.sh` and remove the line `docker rmi $repository/load-balancer-app:latest` to prevent the removal of the created images.
4. Run the following command: ```bash runbuild.sh -v 20 -r "dockerhub repo name" -p "dockerhub password" -e "environment name" -u "dockerhub username"```. You can pass an empty string (" ") as the username and password if you don't want to push the image to Dockerhub (it will give an error, but the images will still be created successfully). Also, the "dockerhub repo name" will be appended to the created image.
5. Now, run the container using ```docker run``` (Expose the port as mentioned in application.properties).


### Steps to run and deploy using Jenkins

--in porgress--
