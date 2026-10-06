FROM xebialabs/xl-deploy:26.3

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
