## Сборка 

```bash
mvn package
```

```bash
MAVEN_OPTS="--enable-native-access=ALL-UNNAMED" mvn -q -pl launcher exec:java -Dexec.args="tests/regression/test001.lama"
```

```bash
MAVEN_OPTS="--enable-native-access=ALL-UNNAMED -Xss128m -Xmx4g" mvn -q -pl launcher exec:java -Dexec.args="tests/performance/Sort.lama"
```

```bash
mvn package -Pnative
```

```bash
./lama tests/regression/test001.lama
```

```bash
./lama tests/performance/Sort.lama
```

## Docker

На всякий случай есть Docker-образ с эталонным lamac и GraalVM:

```bash
docker run --platform=linux/amd64 --rm -v $PWD:/lama -it -w /lama trickyfoxy/lama_truffle bash
```

Собирается он так:

```bash
docker build --platform=linux/amd64 -t trickyfoxy/lama_truffle .
```

