package graphtutorial;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.MessageCollectionPage;
import com.microsoft.graph.requests.UserCollectionPage;

public class App {
    public static void main(String[] args) {
        System.out.println("Java Graph Tutorial");
        System.out.println();

        final Properties oAuthProperties = new Properties();
        try {
            oAuthProperties.load(App.class.getResourceAsStream("oAuth.properties"));
        } catch (IOException e) {
            System.out.println("Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
            return;
        }

        initializeGraph(oAuthProperties);

        greetUser();

        Scanner input = new Scanner(System.in);

        int choice = -1;

        while (choice != 0) {
            System.out.println("Please choose one of the following options:");
            System.out.println("0. Exit");
            System.out.println("1. Display access token");
            System.out.println("2. List my inbox");
            System.out.println("3. Send mail");
            System.out.println("4. List Folders");
            System.out.println("5. Get Excel File");

            try {
                choice = input.nextInt();
            } catch (InputMismatchException ex) {
                // Skip over non-integer input
            }

            input.nextLine();

            // Process user choice
            switch (choice) {
                case 0:
                    // Exit the program
                    System.out.println("Goodbye...");
                    break;
                case 1:
                    // Display access token
                    displayAccessToken();
                    break;
                case 2:
                    // List emails from user's inbox
                    listInbox();
                    break;
                case 3:
                    // Send an email message
                    sendMail();
                    break;
                case 4:
                    // Run any Graph code
                    listFolders();
                    break;
                case 5:
                    getExcelFile();
                    break;
                default:
                    System.out.println("Invalid choice");
            }
        }
        input.close();
    }

    private static void greetUser() {
        try {
            final User user = Graph.getUser();
            // For Work/school accounts, email is in mail property
            // Personal accounts, email is in userPrincipalName
            final String email = user.mail == null ? user.userPrincipalName : user.mail;
            System.out.println("Hello, " + user.displayName + "!");
            System.out.println("Email: " + email);
        } catch (Exception e) {
            System.out.println("Error getting user");
            e.printStackTrace();
        }
    }

    private static void displayAccessToken() {
        try {
            final String accessToken = Graph.getUserToken();
            System.out.println("Access token: " + accessToken);
        } catch (Exception e) {
            System.out.println("Error getting access token");
            System.out.println(e.getMessage());
        }
    }

    private static void listInbox() {
        try {
            final MessageCollectionPage messages = Graph.getInbox();

            // Output each message's details
            for (Message message : messages.getCurrentPage()) {
                System.out.println("Message: " + message.subject);
                System.out.println("  From: " + message.from.emailAddress.name);
                System.out.println("  Status: " + (message.isRead ? "Read" : "Unread"));
                System.out.println("  Received: " + message.receivedDateTime
                        // Values are returned in UTC, convert to local time zone
                        .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
            }

            final Boolean moreMessagesAvailable = messages.getNextPage() != null;
            System.out.println("\nMore messages available? " + moreMessagesAvailable);
        } catch (Exception e) {
            System.out.println("Error getting inbox");
            System.out.println(e.getMessage());
        }
    }

    private static void sendMail() {
        try {
            // Send mail to the signed-in user
            // Get the user for their email address
            final User user = Graph.getUser();
            final String email = user.mail == null ? user.userPrincipalName : user.mail;

            Graph.sendMail("Testing Microsoft Graph", "Hello world!", email);
            System.out.println("\nMail sent.");
        } catch (Exception e) {
            System.out.println("Error sending mail");
            System.out.println(e.getMessage());
        }
    }

    private static void listFolders() {
        try {
            DriveItemCollectionPage result = Graph.listFolders();
            for (DriveItem item : result.getCurrentPage()) {
                System.out.println("Name: " + item.name);
                System.out.println("ID: " + item.id);
                System.out.println("Last Modified: " + item.lastModifiedDateTime + "\n");
            }
        } catch (Exception e) {
            System.out.println("Error making Graph call");
            System.out.println(e.getMessage());
        }
    }

    private static void getExcelFile() {
        try {
            Workbook result = Graph.getExcelFile();
            System.out.println("Name: " + result.names);
            System.out.println("ID: " + result.id);
            System.out.println("Last Modified: " + result.oDataType + "\n");
        } catch (Exception e) {
            System.out.println("Error making Graph call");
            e.printStackTrace();
        }
    }

    private static void initializeGraph(Properties properties) {
        try {
            Graph.initializeGraphForUserAuth(properties,
                    challenge -> System.out.println(challenge.getMessage()));
        } catch (Exception e) {
            System.out.println("Error initializing Graph for user auth");
            System.out.println(e.getMessage());
        }
    }

    private static void listUsers() {
        try {
            final UserCollectionPage users = Graph.getUsers();

            // Output each user's details
            for (User user: users.getCurrentPage()) {
                System.out.println("User: " + user.displayName);
                System.out.println("  ID: " + user.id);
                System.out.println("  Email: " + user.mail);
            }

            final Boolean moreUsersAvailable = users.getNextPage() != null;
            System.out.println("\nMore users available? " + moreUsersAvailable);
        } catch (Exception e) {
            System.out.println("Error getting users");
            System.out.println(e.getMessage());
        }
    }
}
