# spring-oauth2 example

## Requirements

* maven
* angular
* docker

## Build it

```
docker-compose --file build.docker-compose.yml down --remove-orphans && docker-compose --file build.docker-compose.yml up --build
```

## Run it

```
docker-compose down --remove-orphans && docker-compose up --build
```

## Links

| URL | Description |
|-----|-------------|
| http://localhost:8888 | spring-oauth2 endpoint |
| http://localhost:80 | angular-oauth2 endpoint |
| http://localhost:8080/auth/ | local keycloak endpoint |
