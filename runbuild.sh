print_messages(){

echo "======================================"
echo "Phase : $1"
echo "======================================"

}

print_messages "Building jar file - In progress"

docker build -t jdk-17-load-balancer:latest .
docker run --name test --rm  -v /target:/target jdk-17-load-balancer:test

print_messages "Building jar file - Completed"

chmod -R 775 /target/app.jar

cp /target/app.jar ./builds/app.jar

print_massages "Building runnnable image - In Progress"

cd builds
docker build -t load-balancer-app:$(app_version) .

print_massages "Building runnnable image - Completed"