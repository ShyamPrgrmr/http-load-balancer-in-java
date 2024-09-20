sudo docker build -t jenkins:latest .

sudo docker run -d --network jenkins --name jenkins-docker -p 8080:8080 --restart on-failure -p 50000:50000 -v /var/run/docker.sock:/var/run/docker.sock -v /home/shyam/projects/jenkins:/var/jenkins_home jenkins
	
sudo docker run -d --restart=always -p 127.0.0.1:2376:2375 --network jenkins -v /var/run/docker.sock:/var/run/docker.sock alpine/socat tcp-listen:2375,fork,reuseaddr unix-connect:/var/run/docker.sock
