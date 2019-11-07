docker build -t geozone:latest .
docker run -d --name geozone -m 900 -p 8860:8860 -e USER_NAME=admin -e USER_PASSWORD=Admin1234 geozone