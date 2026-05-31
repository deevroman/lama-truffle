## JVM-сборка 

```bash
mvn package
```

```bash
./sl  tests/regression/test007.lama
```

Запуск всех тестов:

```bash
./run-regression-tests.sh
```

Если тесты упали из-за варнингов, то запускать то запускать так:

```bash
JAVA_UNSAFE_MEMORY_ACCESS_ARG= ./run-regression-tests.sh
```

## Сборка в нативный образ

```bash
mvn package -Pnative
```

```bash
./launcher/target/lama tests/regression/test001.lama
```

```bash
./launcher/target/lama tests/performance/Sort.lama
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
