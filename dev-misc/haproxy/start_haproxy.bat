docker stop haproxy
docker rm haproxy

REM <local haproxy config directory> must be repaced with a full path to a directory on the local developer's machine
REM containing files haproxy.cfg and haproxy.pem
REM for example, /home/username/haproxy
docker run -d -p 80:80 -p 443:443 -p 9000:9000 --name haproxy --restart=always -v /c/Users/username/haproxy:/usr/local/etc/haproxy:ro haproxy:1.9.8
timeout 2

echo "------------------------------------------------------------------------------"

docker logs -f haproxy