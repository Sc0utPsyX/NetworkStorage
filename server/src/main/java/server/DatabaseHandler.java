package server;

import files.RegistrationMessage;

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

    public static boolean registerUser(RegistrationMessage msg)  {
        String sql = "INSERT INTO users (username, password, directory, mail) VALUES " +
                "(?, ?, ?, ?)";
        try {
        PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, msg.getName());
            stmt.setString(2, msg.getPassword());
            stmt.setString(3, msg.getName());
            stmt.setString(4, msg.getEmail());
            return stmt.executeUpdate() != -1;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
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
