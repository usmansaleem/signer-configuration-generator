# signer-configuration-generator

Utility to generate a large number of Web3Signer configuration files with random keys.
 - Web3Signer encrypted, raw files and hashicorp loading (BLS Keys).

## build application:
~~~
./gradlew clean build installdist
~~~

## cd to install distribution
~~~
cd ./build/install/signer-configuration-generator/bin
~~~

## run application

### Web3Signer Raw configuration files generation
~~~
./signer-configuration-generator raw --count=10000
~~~

### Web3Signer Hashicorp configuration files generation
- Note: Run Hashicorp vault in dev mode (via Docker)
~~~
docker pull vault
docker run --rm --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -p 8200:8200 --name=dev-vault vault
~~~
~~~
./signer-configuration-generator hashicorp --count=10000 --token=myroot
~~~

