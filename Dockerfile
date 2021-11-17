FROM amazoncorretto:11-alpine-jdk

WORKDIR /bot

COPY local.db ./local.db
COPY target/subscribers-bot-0.1.0-standalone.jar ./bot.jar

CMD java -jar bot.jar
