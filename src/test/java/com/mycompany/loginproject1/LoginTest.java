package com.mycompany.loginproject1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 1 regression tests: the login functionality must still work. */
class LoginTest {
    private final Login login = new Login();

    @Test
    void usernameMustHaveUnderscoreAndAtMostFiveCharacters() {
        assertTrue(login.checkUserName("kyl_1"));
        assertFalse(login.checkUserName("kyle!!!!!!"));
        assertFalse(login.checkUserName("kyle1"));
        assertFalse(login.checkUserName(null));
    }

    @Test
    void passwordRequiresCapitalNumberSpecialAndLength() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
        assertFalse(login.checkPasswordComplexity("password"));
        assertFalse(login.checkPasswordComplexity("Passw0rd"));
        assertFalse(login.checkPasswordComplexity(null));
    }

    @Test
    void registrationPhoneMustHaveActualDigitsAndInternationalCode() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
        assertFalse(login.checkCellPhoneNumber("08966553"));
        assertFalse(login.checkCellPhoneNumber("+27abcdefghi"));
        assertFalse(login.checkCellPhoneNumber(null));
    }

    @Test
    void successfulRegistrationAndLoginHaveExpectedMessages() {
        assertEquals("User registered successfully.",
                login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith"));
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
        assertEquals("Welcome Kyle Smith, it is great to see you again.",
                login.returnLoginStatus("kyl_1", "Ch&&sec@ke99!"));
        assertFalse(login.loginUser("wrong", "wrong"));
    }

    @Test
    void rejectedRegistrationDoesNotPermitLogin() {
        assertTrue(login.registerUser("badname", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith")
                .startsWith("Username is not correctly formatted"));
        assertFalse(login.loginUser("badname", "Ch&&sec@ke99!"));
    }

    @Test
    void invalidNewRegistrationClearsPreviousCredentials() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        login.registerUser("badname", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertFalse(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }
}
