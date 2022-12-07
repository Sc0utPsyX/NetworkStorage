package server;

import java.sql.*;

public class DatabaseHandler {
    private static Connection connection;
    private static Statement statement;


    public static String readUserDatabase(String name, String password){
        try(ResultSet resultSet = statement.executeQuery("SELECT * FROM users")){
            if (resultSet == null){
                return null;
            }
            while (resultSet.next()){
                if (resultSet.getString("username").equals(name) && resultSet.getString("password").equals(password)){
                    return resultSet.getString("directory");
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }


    public static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:server/src/main/java/server/Users.db");
        statement = connection.createStatement();
    }

    public static void disconnect(){
        if (statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
