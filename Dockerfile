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

# Run tests in headless mode, then generate the Allure HTML report.
# Results are written to /app/target/allure-results (mounted as a volume
# in docker-compose so the allure-serve container can read them).
ENV SELENIDE_HEADLESS=true

CMD mvn verify; allure generate target/allure-results --clean -o /allure-report
