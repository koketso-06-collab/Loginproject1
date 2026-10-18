package com.mycompany.loginproject1;

public class QuickChat {
    public static void main(String[] args) {
        Login myLogin = new Login();
        
        String result = myLogin.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        System.out.println(result);
        
        String status = myLogin.returnLoginStatus("kyl_1", "Ch&&sec@ke99!");
        System.out.println(status);
    }
}
