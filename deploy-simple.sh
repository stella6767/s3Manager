#!/bin/bash
# deploy-simple.sh

KEY_FILE="~/.ssh/gcp"
TARGET_SERVER="freeapp1723@34.64.176.98"
TARGET_PATH="/home/freeapp1723/cicd/s3manager"

echo "Building JAR file..."
./gradlew clean build

echo "Creating target directory..."
ssh -i $KEY_FILE $TARGET_SERVER "mkdir -p $TARGET_PATH/build/libs"

echo "Transferring files..."
scp -i $KEY_FILE Dockerfile $TARGET_SERVER:$TARGET_PATH/
scp -i $KEY_FILE -r build/libs/ $TARGET_SERVER:$TARGET_PATH/build

echo "Building and running on target server..."
ssh -i $KEY_FILE $TARGET_SERVER << EOF
cd $TARGET_PATH
sudo docker build -t s3manager:latest .
sudo docker stop s3manager-container 2>/dev/null || true
sudo docker rm s3manager-container 2>/dev/null || true
sudo docker run -d -p 8083:8083 --name s3manager-container s3manager:latest
EOF

echo "Deployment completed!"
