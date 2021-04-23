# web3signer-hashicorp-loader

Utility to generate a large number of BLS Key Pair, insert them in Hashicorp Vault and create
Web3Signer configuration files for them.

## Run Hashicorp vault in dev mode (docker)
- `docker pull vault`
- `docker run --rm --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -p 8200:8200 --name=dev-vault vault`

## build application:
- Note: _Google Code Format plugin has issues with JDK 16_
~~~
./gradlew clean build installdist
cd ./app/build/install/app/bin

~~~