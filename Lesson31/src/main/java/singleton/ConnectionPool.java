package by.teachmeskills.patterns.singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPool{

    //Singleton instance
    private static volatile ConnectionPool instance;

    //Configuration properties
    private static final String DB_PROPERTY_FILE = "application";
    private static final String DB_URL = "db.url";
    private static final String DB_LOGIN = "db.login";
    private static final String DB_PASS = "db.pass";
    private static final int MAX_CONNECTION_COUNT = 10;
    private static final int MIN_CONNECTION_COUNT = 5;

    private static final String URL;
    private static final String LOGIN;
    private static final String PASS;

    private static final Object LOCK = new Object();

    static {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(DB_PROPERTY_FILE);
        URL = resourceBundle.getString(DB_URL);
        LOGIN = resourceBundle.getString(DB_LOGIN);
        PASS = resourceBundle.getString(DB_PASS);
    }

//    private volatile int currentConnectionNumber = MIN_CONNECTION_COUNT;

    private final AtomicInteger currentConnectionNumber = new AtomicInteger(MIN_CONNECTION_COUNT);
    private final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(MAX_CONNECTION_COUNT, true);

    //Singleton
    public static ConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }

    //Add new connection to queue in constructor
    private ConnectionPool() {
        for (int i = 0; i < MIN_CONNECTION_COUNT; i++) {
            try {
                pool.add(DriverManager.getConnection(URL, LOGIN, PASS));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void openAdditionalConnection() throws Exception {
        try {
            pool.add(DriverManager.getConnection(URL, LOGIN, PASS));
//            synchronized (LOCK) {
                currentConnectionNumber.incrementAndGet();
//            }
        } catch (SQLException e) {
            throw new Exception("New connection wasn't add in the connection pool", e);
        }
    }

    public Connection getConnection() throws Exception {
        Connection connection;
        try {
            if (pool.isEmpty() && currentConnectionNumber.get() < MAX_CONNECTION_COUNT) {
                openAdditionalConnection();
            }
            connection = pool.take();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new Exception("Max count of connections was reached!");
        }

        return connection;
    }

    public void closeConnection(Connection connection) throws Exception {
        if (connection != null) {
            if (currentConnectionNumber.get() > MIN_CONNECTION_COUNT) {
//                synchronized (LOCK) {
                    currentConnectionNumber.decrementAndGet();
//                }
            }
            try {
                pool.put(connection);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Exception("Connection wasn't returned into pool properly");
            }
        }
    }
}
