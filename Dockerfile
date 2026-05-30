FROM trickyfoxy/lama

ARG GRAALVM_VERSION=25.0.2
ARG GRAALVM_TARBALL=graalvm-community-jdk-${GRAALVM_VERSION}_linux-x64_bin.tar.gz
ARG GRAALVM_URL=https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${GRAALVM_VERSION}/${GRAALVM_TARBALL}

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

USER root

ENV MAVEN_REPO=/opt/maven/repository
ENV MAVEN_OPTS=-Dmaven.repo.local=/opt/maven/repository

RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        maven \
        tar \
        gzip \
        build-essential \
        zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "${GRAALVM_URL}" -o /tmp/${GRAALVM_TARBALL} \
    && mkdir -p /opt/graalvm \
    && tar -xzf /tmp/${GRAALVM_TARBALL} -C /opt/graalvm --strip-components=1 \
    && rm /tmp/${GRAALVM_TARBALL}

RUN mkdir -p /opt/maven/repository /tmp/lama-truffle \
    && chown -R user:user /opt/maven /tmp/lama-truffle

ENV JAVA_HOME=/opt/graalvm
ENV PATH=/opt/graalvm/bin:/usr/share/maven/bin:${PATH}

COPY --chown=user:user . /tmp/lama-truffle

USER user

RUN cd /tmp/lama-truffle && mvn -B test

WORKDIR /lama

RUN java -version && mvn -version

CMD ["bash"]
