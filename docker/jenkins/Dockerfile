FROM jenkins/jenkins:lts-jdk21

USER root

COPY certs/ /tmp/company-certs/

RUN set -eux; \
    find /tmp/company-certs -maxdepth 1 -type f -name '*.crt' \
        -exec cp {} /usr/local/share/ca-certificates/ \; ; \
    update-ca-certificates; \
    for cert in /usr/local/share/ca-certificates/*.crt; do \
        [ -f "$cert" ] || continue; \
        alias="$(basename "$cert" .crt)"; \
        keytool -importcert \
            -noprompt \
            -trustcacerts \
            -alias "$alias" \
            -file "$cert" \
            -keystore "$JAVA_HOME/lib/security/cacerts" \
            -storepass changeit; \
    done; \
    rm -rf /tmp/company-certs

RUN apt-get update \
    && apt-get install -y maven \
    && rm -rf /var/lib/apt/lists/*

USER jenkins
