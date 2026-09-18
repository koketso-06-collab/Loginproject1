import com.mycompany.loginproject1.Login;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {
    Login login = new Login("Kyle","Graham");
    @Test public void testCheckUserNameCorrectlyFormatted(){ assertTrue(login.checkUserName("kyl_1")); }
    @Test public void testCheckUserNameIncorrectlyFormatted(){ assertFalse(login.checkUserName("kyle!!!!!!")); }
    @Test public void testCheckPasswordComplexityCorrect(){ assertTrue(login.checkPasswordComplexity("Ch&sec@ke99!")); }
    @Test public void testCheckPasswordComplexityIncorrect(){ assertFalse(login.checkPasswordComplexity("password")); }
    @Test public void testCheckCellPhoneCorrect(){ assertTrue(login.checkCellPhoneNumber("+27838968976")); }
    @Test public void testCheckCellPhoneIncorrect(){ assertFalse(login.checkCellPhoneNumber("08966553")); }
}
