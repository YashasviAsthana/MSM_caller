package com.projects.mara.msm_caller;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SignIn extends AppCompatActivity {

    TextInputEditText in_username,in_pass;
    TextView signup;
    Button login,forgotpass;

    private boolean loggedIn = false;
    Intent registerPage;
    Intent mainPage;
    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        registerPage = new Intent(this,Register.class);
        mainPage = new Intent(this,MainActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(SignIn.this,
                    Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED | ActivityCompat.checkSelfPermission(SignIn.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_CONTACTS}, 0x12345);
            }
        }
        getSupportActionBar().setTitle("Sign In");
        in_username = (TextInputEditText) findViewById(R.id.username);
        in_pass = (TextInputEditText) findViewById(R.id.password);
        login = (Button)findViewById(R.id.signInBtn);
        signup = (TextView)findViewById(R.id.signup);
        forgotpass = (Button)findViewById(R.id.forgot);


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(registerPage);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check user name and pass then intent to main page
                login();
            }
        });
        final Intent forgotpasspage = new Intent(this,ForgotPass.class);
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(forgotpasspage);
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        if(isAccessibilityServiceEnabled(SignIn.this,USSDService.class)){
            Log.i("Service: ","Fine");
        }
        else{
            Log.i("Service: ","Not Running");
            FragmentManager service = getFragmentManager();
            AccessibilityDialog serviceFragment = new AccessibilityDialog();
            serviceFragment.show(service, "theDialog");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME,Context.MODE_PRIVATE);

        loggedIn = sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF,false);

        if(loggedIn){
            finish();
            startActivity(mainPage);
        }
    }

    private void login(){

        final String username = in_username.getText().toString().trim();
        final String password = in_pass.getText().toString().trim();
        //Toast.makeText(SignIn.this,"HERE",Toast.LENGTH_SHORT).show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase(Config.LOGIN_SUCCESS)) {
                            SharedPreferences sharedPreferences = SignIn.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
                            editor.putString(Config.USER_SHARED_PREF, username);
                            editor.apply();
                            finish();
                            startActivity(mainPage);
                        } else {
                            Toast.makeText(SignIn.this, "Invalid Credentials.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Handle error
                        Toast.makeText(SignIn.this,"Unable to connect to the Server.",Toast.LENGTH_SHORT).show();
                    }
                }){


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put(Config.KEY_USERNAME,username);
                params.put(Config.KEY_PASSWORD,password);

                return params;
            }

            /*
            @Override
            public byte[] getBody() throws com.android.volley.AuthFailureError {
                String str = "{\"username\":\""+username+"\",\"password\":\""+password+"\"}";
                return str.getBytes();
            };

            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
            }

            */

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }





    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }


}
