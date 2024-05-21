package com.elevenware.jdbc.fyeo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class JdbcFyeoDriver implements Driver {
    private SecretStore secretStore;

    public JdbcFyeoDriver() {
    }

    public JdbcFyeoDriver(SecretStore secretStore) {
        this.secretStore = secretStore;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if(!url.startsWith("jdbc:secret")) {
            return null;
        }
        String[] urlParts = url.split(":");
        StringBuilder actual = new StringBuilder().append("jdbc");
        String secretImpl = urlParts[1];

        for(int i = 2; i < urlParts.length; i++) {
            actual.append(":").append(urlParts[i]);
        }
        SecretStore store = secretStore(secretImpl);
        String userName = store.resolve(info.getProperty("user"));
        String password = store.resolve(info.getProperty("password"));
        info.setProperty("user", userName);
        info.setProperty("password", password);
        return DriverManager.getConnection(actual.toString(), info);
    }

    private SecretStore secretStore(String secretImpl) {
        if(this.secretStore != null) {
            return secretStore;
        }
        if(!secretImpl.contains(".")) {
            return InMemorySecretStore.getInstance();
        }
        secretImpl = secretImpl.split("\\.")[1];
        switch(secretImpl) {
            case "aws":
                return loadStore(SecretStoreDrivers.AWS_SECRET_STORE);
        }
        throw new JdbcFyeoException(String.format("Cannot load secret driver for %s", secretImpl));
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    private SecretStore loadStore(String className) {
        try {
            Class<? extends SecretStore> clazz = (Class<? extends SecretStore>) Class.forName(className);
            Constructor<? extends SecretStore> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
