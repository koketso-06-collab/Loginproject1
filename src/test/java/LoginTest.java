package com.mycompany.loginproject1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {
    
    Login login = new Login();
    
    @Test
    public void testCheckUserName() {
        System.out.println("Testing username...");
        assertTrue(login.checkUserName("kyl_1"));
        assertFalse(login.checkUserName("kyle!!!!!!"));
    }
    
    @Test
    public void testCheckPassword() {
        System.out.println("Testing password...");
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
        assertFalse(login.checkPasswordComplexity("password"));
    }
    
    @Test
    public void testCheckCell() {
        System.out.println("Testing cell...");
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }
    
    @Test
    public void testRegister() {
        System.out.println("Testing registration...");
        String result = login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertEquals("User registered successfully.", result);
    }
    
    @Test
    public void testLogin() {
        System.out.println("Testing login...");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
        assertFalse(login.loginUser("wrong", "wrong"));
    }
}
