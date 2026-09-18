package com.mycompany.loginproject1;
import javax.swing.JOptionPane;
public class Loginproject1 {
    public static void main(String[] args) {
        String firstName = JOptionPane.showInputDialog("Enter First Name:");
        String lastName = JOptionPane.showInputDialog("Enter Last Name:");
        Login login = new Login(firstName, lastName);
        String username = JOptionPane.showInputDialog("Enter Username (e.g. kyl_1):");
        String password = JOptionPane.showInputDialog("Enter Password (e.g. Ch&sec@ke99!):");
        String cell = JOptionPane.showInputDialog("Enter Cell (e.g. +27838968976):");
        String regResult = login.registerUser(username, password, cell);
        JOptionPane.showMessageDialog(null, regResult);
        if (regResult.equals("User registered successfully")) {
            String userLogin = JOptionPane.showInputDialog("Now Login - Enter username:");
            String passLogin = JOptionPane.showInputDialog("Enter password:");
            JOptionPane.showMessageDialog(null, login.returnLoginStatus(userLogin, passLogin));
        }
    }
}
