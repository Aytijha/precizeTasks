import java.sql.*;
import java.util.Scanner;

public class SATResultsApplication {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sat_results";
    private static final String DB_USERNAME = "your_username";
    private static final String DB_PASSWORD = "your_password";

    private static Connection connection;

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            createTableIfNotExists();

            Scanner scanner = new Scanner(System.in);
            int choice;

            do {
                displayMenu();
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                switch (choice) {
                    case 1:
                        try {
                            insertData(scanner);
                        } catch (SQLException e) {
                            System.out.println("An error occurred while inserting data: " + e.getMessage());
                        }
                        break;
                    case 2:
                        try {
                            viewAllData();
                        } catch (SQLException e) {
                            System.out.println("An error occurred while retrieving data: " + e.getMessage());
                        }
                        break;
                    case 3:
                        try {
                            getRank(scanner);
                        } catch (SQLException e) {
                            System.out.println("An error occurred while retrieving rank: " + e.getMessage());
                        }
                        break;
                    case 4:
                        try {
                            updateScore(scanner);
                        } catch (SQLException e) {
                            System.out.println("An error occurred while updating SAT score: " + e.getMessage());
                        }
                        break;
                    case 5:
                        try {
                            deleteRecord(scanner);
                        } catch (SQLException e) {
                            System.out.println("An error occurred while deleting the record: " + e.getMessage());
                        }
                        break;
                    case 0:
                        System.out.println("Exiting the application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            } while (choice != 0);

            connection.close();
        } catch (SQLException e) {
            System.out.println("An error occurred while connecting to the database: " + e.getMessage());
        }
    }

    private static void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS sat_results (" +
                "name VARCHAR(100) PRIMARY KEY," +
                "address VARCHAR(100)," +
                "city VARCHAR(100)," +
                "country VARCHAR(100)," +
                "pincode VARCHAR(20)," +
                "sat_score INT," +
                "passed BOOLEAN" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            System.out.println("An error occurred while creating table: " + e.getMessage());
        }
    }

    private static void displayMenu() {
        System.out.println("SAT Results Application");
        System.out.println("=======================");
        System.out.println("1. Insert data");
        System.out.println("2. View all data");
        System.out.println("3. Get rank");
        System.out.println("4. Update score");
        System.out.println("5. Delete one record");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void insertData(Scanner scanner) throws SQLException {
        System.out.println("Insert Data");
        System.out.println("===========");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        if (recordExists(name)) {
            System.out.println("Record with the same name already exists.");
            return;
        }

        System.out.print("Address: ");
        String address = scanner.nextLine();

        System.out.print("City: ");
        String city = scanner.nextLine();

        System.out.print("Country: ");
        String country = scanner.nextLine();

        System.out.print("Pincode: ");
        String pincode = scanner.nextLine();

        System.out.print("SAT Score: ");
        int satScore = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        boolean passed = satScore > 30;

        String query = "INSERT INTO sat_results (name, address, city, country, pincode, sat_score, passed) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, address);
            statement.setString(3, city);
            statement.setString(4, country);
            statement.setString(5, pincode);
            statement.setInt(6, satScore);
            statement.setBoolean(7, passed);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully.");
            }
        }
    }

    private static void viewAllData() throws SQLException {
        System.out.println("View All Data");
        System.out.println("=============");

        String query = "SELECT * FROM sat_results";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (!resultSet.next()) {
                System.out.println("No data available.");
                return;
            }

            do {
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                String city = resultSet.getString("city");
                String country = resultSet.getString("country");
                String pincode = resultSet.getString("pincode");
                int satScore = resultSet.getInt("sat_score");
                boolean passed = resultSet.getBoolean("passed");

                System.out.println("Name: " + name);
                System.out.println("Address: " + address);
                System.out.println("City: " + city);
                System.out.println("Country: " + country);
                System.out.println("Pincode: " + pincode);
                System.out.println("SAT Score: " + satScore);
                System.out.println("Passed: " + passed);
                System.out.println();
            } while (resultSet.next());
        }
    }

    private static void getRank(Scanner scanner) throws SQLException {
        System.out.println("Get Rank");
        System.out.println("========");

        System.out.print("Enter the name: ");
        String name = scanner.nextLine();

        String query = "SELECT COUNT(*) AS rank FROM sat_results WHERE sat_score > ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, getSatScoreByName(name));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int rank = resultSet.getInt("rank") + 1;
                    System.out.println(name + " has a rank of " + rank);
                } else {
                    System.out.println("Record with the given name does not exist.");
                }
            }
        }
    }

    private static void updateScore(Scanner scanner) throws SQLException {
        System.out.println("Update Score");
        System.out.println("============");

        System.out.print("Enter the name: ");
        String name = scanner.nextLine();

        if (!recordExists(name)) {
            System.out.println("Record with the given name does not exist.");
            return;
        }

        System.out.print("Enter the new SAT Score: ");
        int newScore = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String query = "UPDATE sat_results SET sat_score = ?, passed = ? WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, newScore);
            statement.setBoolean(2, newScore > 30);
            statement.setString(3, name);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Score updated successfully.");
            }
        }
    }

    private static void deleteRecord(Scanner scanner) throws SQLException {
        System.out.println("Delete Record");
        System.out.println("=============");

        System.out.print("Enter the name: ");
        String name = scanner.nextLine();

        if (!recordExists(name)) {
            System.out.println("Record with the given name does not exist.");
            return;
        }

        String query = "DELETE FROM sat_results WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Record deleted successfully.");
            }
        }
    }

    private static boolean recordExists(String name) throws SQLException {
        String query = "SELECT name FROM sat_results WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static int getSatScoreByName(String name) throws SQLException {
        String query = "SELECT sat_score FROM sat_results WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("sat_score");
                } else {
                    throw new SQLException("Record with the given name does not exist.");
                }
            }
        }
    }
}
