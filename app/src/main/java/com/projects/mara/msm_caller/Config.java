package com.projects.mara.msm_caller;

/**
 * Created by Yashasvi on 23-06-2017.
 */

public class Config {

    //server addresses
    //public static final String LOGIN_URL = "http://10.0.1.33/msm_caller/Login.php";
    //public static final String REG_URL = "http://10.0.1.33/msm_caller/AddUser.php";

    public static final String LOGIN_URL = "http://10.0.1.12:8080/CallingApp/login";
    public static final String REG_URL = "http://10.0.1.12:8080/CallingApp/register";

    //as in database
    public static final String KEY_NAME = "name";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";

    //response from the api
    public static final String REG_SUCCESS = "Success";
    public static final String USER_EXISTS = "User Exists";
    public static final String LOGIN_SUCCESS = "Success";


    public static final String SHARED_PREF_NAME = "MSMuser";
    public static final String NAME_SHARED_PREF = "name";
    public static final String USER_SHARED_PREF = "username";
    public static final String ECONTACT_SHARED_PREF = "econtact";
    public static final String LOGGEDIN_SHARED_PREF = "loggedin";
}
