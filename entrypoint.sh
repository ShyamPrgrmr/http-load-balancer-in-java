set -e

print_message() {
  echo "========================================"
  echo "$1"
  echo "========================================"
}

print_message "Cleaning the project"
mvn clean install -U

print_message "Compiling the project"
mvn compile

print_message "Running tests"
mvn test

print_message "Packaging the project"
mvn package

ls /load-balancer/target

chmod -R 777 /target
cp /load-balancer/target/app-0.0.1-SNAPSHOT.jar /target/app.jar
