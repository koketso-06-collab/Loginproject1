package com.mycompany.loginproject1;

/**
 * Part 1 registration and login, retained through Parts 2 and 3.
 * The method names and result messages follow the original Loginproject1 code.
 */
public class Login {
    private String username;
    private String password;
    private String cellNumber;
    private String firstName;
    private String lastName;
    private boolean registered;

    public Login() {
    }

    // Kept for compatibility with the original Part 1 class.
    public Login(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Kept for compatibility with the student's previous Part 2 class.
    public Login(String username, String password, String cellNumber,
                 String firstName, String lastName) {
        registerUser(username, password, cellNumber, firstName, lastName);
    }

    public boolean checkUserName(String username) {
        return username != null
                && username.length() <= 5
                && username.matches("[A-Za-z0-9_]+")
                && username.contains("_");
    }

    public boolean checkPasswordComplexity(String password) {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^A-Za-z0-9].*");
    }

    /** Matches the South African international-format example from Part 1. */
    public boolean checkCellPhoneNumber(String cell) {
        return cell != null && cell.matches("\\+27\\d{9}");
    }

    public String registerUser(String username, String password, String cell,
                               String firstName, String lastName) {
        // Validate BEFORE saving credentials: invalid registration cannot log in.
        registered = false;
        this.username = null;
        this.password = null;

        if (!checkUserName(username)) {
            return "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than 5 characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted, please ensure that the password contains at least 8 characters, a capital letter, a number and a special character.";
        }
        if (!checkCellPhoneNumber(cell)) {
            return "Cell phone number is incorrectly formatted or does not contain an international code.";
        }
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            return "Please enter your first name and last name.";
        }

        this.username = username;
        this.password = password;
        this.cellNumber = cell;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        registered = true;
        return "User registered successfully.";
    }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " "
                + (lastName == null ? "" : lastName);
    }

    /** Part 1-compatible overload that uses the names supplied in the constructor. */
    public String registerUser(String username, String password, String cell) {
        return registerUser(username, password, cell, firstName, lastName);
    }

    public boolean loginUser(String enteredUsername, String enteredPassword) {
        return registered && username.equals(enteredUsername)
                && password.equals(enteredPassword);
    }

    public String returnLoginStatus(String enteredUsername, String enteredPassword) {
        if (loginUser(enteredUsername, enteredPassword)) {
            return "Welcome " + firstName + " " + lastName
                    + ", it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }
}
