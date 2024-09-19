#!/bin/bash

workdir=$(pwd)  
cd /target
echo "generated files are : "
ls -ltr

filepath="$workdir/builds/app.jar" 

echo "Copying file from $PWD/app.jar to $filepath"
cp ./app.jar "$filepath"
