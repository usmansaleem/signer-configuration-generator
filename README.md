# signer-configuration-generator

Utility to generate a large number of Web3Signer and EthSigner configuration files with random keys.
 - Web3Signer raw files and hashicorp loading (BLS Keys).
 - Supports EthSigner file-based-signer (SECP V3 Keystore)

## build application:
~~~
./gradlew clean build installdist
cd ./build/install/signer-configuration-generator
~~~

## run application
### Raw configuration files generation
~~~
./signer-configuration-generator raw --count=10000
~~~
### Hashicorp configuration files generation
- Note: Run Hashicorp vault in dev mode (via Docker)
~~~
docker pull vault
docker run --rm --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -p 8200:8200 --name=dev-vault vault
~~~
~~~
./signer-configuration-generator hashicorp --count=10000 --token=myroot
~~~

### EthSigner configuration files generation
~~~
./signer-configuration-generator ethsigner --count=10 --password=password
~~~
