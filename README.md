# web3signer-hashicorp-loader

Utility to generate a large number of BLS Key Pair and corresponding Web3Signer Configuration files.
Supports raw files and hashicorp loading.

## Run Hashicorp vault in dev mode (docker)
- `docker pull vault`
- `docker run --rm --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -p 8200:8200 --name=dev-vault vault`

## build application:
~~~
./gradlew clean build installdist
cd ./app/build/install/app/bin
~~~

## run application
~~~

~~~