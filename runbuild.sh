#!/bin/bash

environments=("test" "prod")

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

    for item in "${environments[@]}"; do
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

workdir=$(pwd)
echo "WORKDIR : $workdir"

#mkdir target

docker build -t load-balancer-app-builder-$version:latest .
docker run --name test --rm  -v $workdir/builds:/target load-balancer-app-builder-$version:latest

print_messages "Building jar file - Completed"

#cd /builds
#echo "generated app.jar : "
#ls -ltr
#filepath="$workdir/builds/app.jar"
#echo "Copying file from $PWD/app.jar to $filepath"
#cp ./app.jar "$filepath"

print_messages "Building application docker image - In Progress"


chmod -R 777 $workdir/builds

whoami
cd builds
echo "generated app.jar : "
pwd
echo "Directory : $workdir/builds"
ls -ltr
ls -ltr $workdir/builds


docker build -t $repository/load-balancer-app:latest .

print_messages "Building docker image - Completed"


print_messages "Pushing image into repository - In Progress"

docker login --username $user --password $password
docker push $repository/load-balancer-app:latest

print_messages "Pushing image into repository - Completed"


print_messages "Cleaning -- In Progress"

echo "Cleaning load-balancer-app-builder-$version:latest image"
docker rmi load-balancer-app-builder-$version:latest

echo "Cleaning $repository/load-balancer-app-$version:latest image"
docker rmi $repository/load-balancer-app:latest

print_messages "Cleaning -- Completed"





