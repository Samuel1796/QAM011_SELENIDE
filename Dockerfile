FROM selenium/standalone-chrome:latest

USER root

# Install Maven, wget, and tar for Allure CLI installation
RUN apt-get update && \
    apt-get install -y maven wget tar && \
    rm -rf /var/lib/apt/lists/*

# Install Allure CLI
ARG ALLURE_VERSION=2.29.0
RUN wget -qO /tmp/allure.tgz "https://github.com/allure-framework/allure2/releases/download/${ALLURE_VERSION}/allure-${ALLURE_VERSION}.tgz" && \
    tar -xzf /tmp/allure.tgz -C /opt && \
    ln -s /opt/allure-${ALLURE_VERSION}/bin/allure /usr/local/bin/allure && \
    rm /tmp/allure.tgz

WORKDIR /app

# Copy project files and pre-download dependencies (cached layer)
COPY . .
RUN mvn dependency:resolve -q

# Pass headless flag as a Maven system property so Selenide reads it via its
# own properties mechanism — consistent with how selenide.properties works locally.
CMD mvn verify -Dselenide.headless=true; allure generate target/allure-results --clean -o /allure-report
