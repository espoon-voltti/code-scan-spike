FROM 307238562370.dkr.ecr.eu-west-1.amazonaws.com/voltti/openjdk:11-jre-slim-latest

LABEL maintainer="https://github.com/espoon-voltti"

USER root

# Set integration username
ENV USERNAME integration
ENV HOME_DIR /home/${USERNAME}
ENV USER_ID 1010

# Create a new user which should be used to run any software.
RUN adduser ${USERNAME} --shell /sbin/nologin --gecos "" -q --home ${HOME_DIR} --uid ${USER_ID} --disabled-password

USER ${USERNAME}
WORKDIR ${HOME_DIR}

# Copy app into container
ENV APP_DIR ./app
ENV APP_USER ${USERNAME}

COPY --chown=${APP_USER}:${APP_USER} target/*.jar ${APP_DIR}/app.jar

# Configure container (eg. expose ports, set timezone)
EXPOSE 8080

WORKDIR ${APP_DIR}
CMD ["java", "-jar", "app.jar"]
