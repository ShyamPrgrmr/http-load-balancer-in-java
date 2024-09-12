#!/bin/bash




environment=("test" "dev" "prod")

while getopts v:u:p:r:e: flag
do
    case "${flag}" in
        r) repository=${OPTARG};;
        v) version=${OPTARG};;
        u) user=${OPTARG};;
        p) password=${OPTARG};;
	e) env=${OPTARG};;
    esac
done


if [ -z "$repository" ]; then
  echo "Dockerhub repository name is not provided (Use -r while running runbuild.sh)"
  exit 1
else
  echo "Docker Hub Repository : $repository"
fi

if [ -z "$version" ]; then
  echo "Version is not provided (Use -v while running runbuild.sh)"
  exit 1
else
  echo "App Version Name : $version"
fi

if [ -z "$user" ]; then
  echo "Username is not provided (Use -u while running runbuild.sh)"
  exit 1
else
  echo "Userame : $user"
fi

if [ -z "$password" ]; then
  echo "Password is not provided (Use -u while running runbuild.sh)"
  exit 1
else
  echo "Login with password"
fi


if [ -z "$env" ]; then
  echo "No deployment environment defined setting it to : test"	    
  env="test"
else
  echo "Selected environemnt to deploy : $env"
fi



function contains() {
    local search="$1"
    shift
    local array=("$@")

    for item in "${environment[@]}"; do
     if [[ "$item" == "$search" ]]; then
       return 0  
     fi
    done
    return 1 
}



if contains "$env" "${my_array[@]}"; then
   echo "_______________________________________________________________________"
else
   echo "Exiting : Deployment environment not found"
fi


print_messages(){

echo "======================================"
echo "Phase : $1"
echo "======================================"

}

print_messages "Building jar file - In progress"

docker build -t load-balancer-app-builder:$version .
docker run --name test --rm  -v /target:/target load-balancer-app-builder:$version

print_messages "Building jar file - Completed"

chmod -R 775 /target/app.jar

cp /target/app.jar ./builds/app.jar

print_messages "Building docker image - In Progress"

cd builds
#docker build -t $repository/load-balancer-app:$version .

print_messages "Building docker image - Completed"


print_messages "Pushing image into repository - In Progress"

docker login --username $user --password $password
#docker push $repository/load-balancer-app:$version

print_messages "Pushing image into repository - Completed"
