# web3signer-hashicorp-loader

Utility to generate a large number of BLS Key Pair and corresponding Web3Signer Configuration files.
Supports raw files and hashicorp loading.

## build application:
~~~
./gradlew clean build installdist
cd ./build/install/web3signer-configuration-generator
~~~

## run application
### Raw configuration files generation
~~~
./web3signer-configuration-generator raw --count=10000
~~~
### Hashicorp configuration files generation
- Note: Run Hashicorp vault in dev mode (via Docker)
~~~
docker pull vault
docker run --rm --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -p 8200:8200 --name=dev-vault vault
~~~
~~~
./web3signer-configuration-generator hashicorp --count=10000 --token=myroot
~~~ 
