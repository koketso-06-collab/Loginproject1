package com.mycompany.loginproject1;

public class Login {
    private String username;
    private String password;
    private String cellNumber;
    private String firstName;
    private String lastName;

    public Login() {}
    
    public Login(String username, String password, String cellNumber, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.cellNumber = cellNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean checkUserName(String username) {
        return username.contains("_") && username.length() <= 5;
    }

    public boolean checkPasswordComplexity(String password) {
        return password.length() >= 8 
            && password.matches(".*[A-Z].*") 
            && password.matches(".*[0-9].*") 
            && password.matches(".*[^a-zA-Z0-9].*");
    }

    public boolean checkCellPhoneNumber(String cell) {
        return cell != null && cell.startsWith("+27") && cell.length() == 12;
    }

    public String registerUser(String u, String p, String c, String f, String l) {
        username = u; 
        password = p; 
        cellNumber = c; 
        firstName = f; 
        lastName = l;
        if (!checkUserName(u)) {
            return "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than 5 characters in length.";
        }
        if (!checkPasswordComplexity(p)) {
            return "Password is not correctly formatted, please ensure that the password contains at least 8 characters, a capital letter, a number and a special character.";
        }
        if (!checkCellPhoneNumber(c)) {
            return "Cell phone number is incorrectly formatted or does not contain an international code.";
        }
        return "User registered successfully.";
    }

    public boolean loginUser(String u, String p) {
        return username != null && username.equals(u) && password.equals(p);
    }

    public String returnLoginStatus(String u, String p) {
        if (loginUser(u, p)) {
            return "Welcome " + firstName + " " + lastName + ", it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }
}
