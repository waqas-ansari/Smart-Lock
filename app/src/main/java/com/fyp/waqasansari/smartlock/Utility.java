package com.fyp.waqasansari.smartlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    private static Pattern pattern;
    private static Matcher matcher;

    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";


    public static boolean validate(String email) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0 ? true: false;
    }
}