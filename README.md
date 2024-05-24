# JDBC - For Your Eyes Only

jdbc-fyeo is a library which allows you to hide away jdbc credentials in a store of some sort.

## The Problem

The usual trick is to have jdbc credentials exposed as environment variables, or in a properties file that gets
provisioned somehow, or, M forbid, just left baked into the source code somewhere.

This is not ideal. This library allows you to put those credentials elsewhere. Such as AWS Secret Manager

## Usage

This *is* a jdbc driver. To use it, simply use the url as follows:

```java
DriverManager.registerDriver(new JdbcFyeoDriver());

var userNameKey = obtainSecretNameForUser();
var passwordKey = obtainSecretNameForPassword();
var uri = "jdbc:secret:h2:~/test";
Connection connection = DriverManager.getConnection(uri, userNameKey, passwordKey);

```

The jdbc uri is constructed using "jdbc:secret:" followed by the usual driver name.

So where do the secrets come from? Well, without any specific driver being specified, there is a simple in-memory store. This is useful
for testing, but absolutely not recommended for any other purpose. You can populate it like this:

```java
DriverManager.registerDriver(new JdbcFyeoDriver());

InMemorySecretStore store = InMemorySecretStore.getInstance();
store.store(obtainSecretNameForUser(), "jamesbond");
store.store(obtainSecretNameForPassword(), "moneypenny");

var userNameKey = obtainSecretNameForUser();
var passwordKey = obtainSecretNameForPassword();
var uri = "jdbc:secret:h2:~/test";
Connection connection = DriverManager.getConnection(uri, userNameKey, passwordKey);

```

## Secret Stores available

### In-memory

For basic testing only. It is used by default, and is a singleton with which you can register your own secrets

### Env

This pulls secrets from environment variables. It can be configured using

```java
var jdbcUrl = "jdbc:secret.env:h2:~/test";
```

### AWS Secret Store

This uses AWS Secrets Manager as the backing store for your credentials

Assuming a SecretManagerClient is configurable, you can simply configure the store in the jdbc url using this notation

```java
var jdbcUrl = "jdbc:secret.aws:h2:~/test";
```

## Works with popular migration tools

There are other projects which attempt this, but when we tried to use them with a popular migration tool, we found specific code which restricted their use to
the paid-for version.

## Examples

A working example of the usage, using testcontainers for AWS and Postgres, can be found [here](https://github.com/georgecodes/jdbc-fyeo/blob/main/aws-flyway-example/src/test/java/com/elevenware/jdbc/fyeo/example/LocalstackTests.java)

The docker-compose file docker-compose-spring-flyway.yml will build an example Spring Boot/Flyway application

A complete example using the Spring Boot application, Flyway, Postgres managed by RDS and AWS secrets manager can be found in the *terraform* directory

In the latter 2 cases, curling the relevant service at /api/v1/users will return some JSON that ultimately was provisioned by Flyway using the FYEO driver.