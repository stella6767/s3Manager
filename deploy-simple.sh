#!/bin/bash
# deploy-simple.sh

KEY_FILE="~/.ssh/gcp"
TARGET_SERVER="freeapp1723@34.64.176.98"
TARGET_PATH="/home/freeapp1723/cicd/b2b"

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
sudo docker build -t b2b:latest .
sudo docker stop b2b-container 2>/dev/null || true
sudo docker rm b2b-container 2>/dev/null || true
sudo docker run -d -p 8080:8080 --name b2b-container b2b:latest
EOF

echo "Deployment completed!"
