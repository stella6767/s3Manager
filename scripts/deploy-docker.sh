whoami

echo " "
echo "========================"
echo "Path move"
echo "========================"

cd "$HOME/cicd/s3"


echo " "
echo "========================"
echo "Docker compose down"
echo "========================"

# 이미 실행 중인 Docker Compose 중지 및 컨테이너 삭제
sudo docker compose -f "$HOME/cicd/s3/docker-compose-s3.yml" down


echo " "
echo "========================"
echo "Docker compose build"
echo "========================"

sudo docker compose -f "$HOME/cicd/s3/docker-compose-s3.yml" build

echo " "
echo "========================"
echo "Docker Compose Up"
echo "========================"


sudo docker compose -f "$HOME/cicd/s3/docker-compose-s3.yml" up -d
