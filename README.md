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

## JVM-сборка 

```bash
git clone git@github.com:deevroman/lama-truffle.git && cd lama-truffle
```

```bash
mvn package
```

```bash
./sl tests/regression/test007.lama
```

Запуск всех тестов:

```bash
./run-regression-tests.sh
```

Если тесты упали из-за варнингов, то запускать то запускать так:

```bash
JAVA_UNSAFE_MEMORY_ACCESS_ARG= ./run-regression-tests.sh
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

## Замеры

Запуск в Github Actions даёт следующее:

```
bash -c "time ./launcher/target/lama tests/performance/Sort.lama"

real	1m31.173s
user	1m11.189s
sys	0m19.867s
```

Для сравнения предыдущие результаты:

```
Running Lama interpreter...

real	9m19.559s
user	9m18.618s
sys	0m0.881s

Running bytecode interpreter...

real	2m32.642s
user	2m27.699s
sys	0m4.687s

Running bytecode interpreter with verifier...

real	2m0.685s
user	1m54.101s
sys	0m3.327s
```