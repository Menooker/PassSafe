package com.passsafe.passsafe;
import java.security.SecureRandom;
/**
 * Created by Menooker on 2017/9/24.
 */

public class PasswordGenerator {
    //choose character from the string
    static String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
    static String RandomPassoword(int len)
    {
        StringBuilder builder=new StringBuilder();
        //secure random number generator
        SecureRandom random = new SecureRandom();
        for(int i=0;i<len;i++)
        {
            builder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return builder.toString();
    }
}
