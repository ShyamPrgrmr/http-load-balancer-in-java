
while getopts v:u:p:r: flag
do
    case "${flag}" in
        r) repository=${OPTARG};;
        v) version=${OPTARG};;
        u) user=${OPTARG};;
        p) password=${OPTARG};;
    esac
done


if [ -z "$repository" ]; then
  echo "Dockerhub repository name is not provided (Use -r while running runbuild.sh)"
else
  echo "Docker Hub Repository : $repository"
fi

if [ -z "$version" ]; then
  echo "Version is not provided (Use -v while running runbuild.sh)"
else
  echo "App Version Name : $version"
fi

if [ -z "$user" ]; then
  echo "Username is not provided (Use -u while running runbuild.sh)"
else
  echo "Userame : $user"
fi

if [ -z "$password" ]; then
  echo "Password is not provided (Use -u while running runbuild.sh)"
else
  echo "Login with password"
fi


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

print_massages "Building docker image - In Progress"

cd builds
docker build -t $repository/load-balancer-app:$version .

print_massages "Building docker image - Completed"


print_massages "Pushing image into repository - In Progress"

docker login --username $user --password $password
docker push $repository/load-balancer-app:$version

print_massages "Pushing image into repository - In Progress"