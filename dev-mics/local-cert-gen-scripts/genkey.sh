echo "Generating keys..."
openssl req \
    -x509 -sha256 \
    -newkey rsa:4096 \
    -days 1024 \
    -nodes \
    -subj "/C=AU/ST=VIC/O=Fielden/CN=localhost" \
    -extensions SAN \
    -reqexts SAN \
    -config <(cat /etc/ssl/openssl.cnf \
            <(printf "\n[SAN]\nsubjectAltName=DNS:localhost,DNS:tgdev.com")) \
    -keyout "localhost.key" \
    -out "localhost.pem"

openssl x509 -in ./localhost.pem -text -noout

echo "Inspect the generated certificate, which should have Subject Alternative Name afer the public key."
read -p "Press enter to continue"

echo "Concatenating keys into haproxy.pem..."
cat ./localhost.pem localhost.key > haproxy.pem

echo "File haproxy.pem should be moved into the HAProxy configuration directory."
read -p "Press enter to exit"