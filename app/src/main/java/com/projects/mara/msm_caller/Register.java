package com.projects.mara.msm_caller;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class Register extends AppCompatActivity {


    EditText name,phone,email,username,pass;
    Button registerBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Register");

        name = (EditText)findViewById(R.id.name);
        phone = (EditText)findViewById(R.id.phone);
        email = (EditText)findViewById(R.id.email);
        username = (EditText)findViewById(R.id.user);
        pass = (EditText)findViewById(R.id.pass);
        registerBtn = (Button)findViewById(R.id.registerFinal);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //verify the details then display registration successful and go to signin
                register();
            }
        });
    }

    private void register(){
        final String NAME = name.getText().toString().trim();
        final String PHONE = phone.getText().toString().trim();
        final String EMAIL = email.getText().toString().trim();
        final String USERNAME = username.getText().toString().trim();
        final String PASS = pass.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REG_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase(Config.REG_SUCCESS)) {
                            Toast.makeText(Register.this,"Registration Successful.",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Register.this,SignIn.class));
                        } else if(response.equalsIgnoreCase(Config.USER_EXISTS)){
                            Toast.makeText(Register.this, "This username is already taken.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Handle error
                        Toast.makeText(Register.this,"Unable to register due to server error.",Toast.LENGTH_SHORT).show();
                    }
                }){

            /*

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put(Config.KEY_USERNAME,USERNAME);
                params.put(Config.KEY_PASSWORD,PASS);
                params.put(Config.KEY_NAME,NAME);
                params.put(Config.KEY_EMAIL,EMAIL);
                params.put(Config.KEY_PHONE,PHONE);

                return params;
            }

            */

            @Override
            public byte[] getBody() throws com.android.volley.AuthFailureError {
                String str = "{\"username\":\""+USERNAME+"\",\"password\":\""+PASS+"\",\"name\":\""+NAME+"\",\"email\":\""+EMAIL+"\",\"phone\":\""+PHONE+"\"}";
                return str.getBytes();
            };

            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
            }



        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }


    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
