FROM selenium/standalone-chrome:latest

USER root

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .

RUN mvn dependency:resolve -q

ENV SELENIDE_BASE_URL=https://www.saucedemo.com
ENV SELENIDE_BROWSER=chrome

CMD ["mvn", "verify"]
