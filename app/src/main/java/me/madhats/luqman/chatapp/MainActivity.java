package me.madhats.luqman.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lambdaworks.crypto.SCryptUtil;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static String jwt_token = "", salt ="";
    private GoogleApiClient client;
    public final static String JWTTOKEN = "com.example.chatapp.jwt_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onLoginClick(View v) throws Exception {
        if (v.getId() == R.id.login_btn) {
            EditText username = (EditText) findViewById(R.id.username_txt);
            TextView tv = (TextView) findViewById(R.id.textView2);
            String input = "{ \"commit\": \"Get Salt\",\"user\":{\"email\": \""+username.getText().toString().trim()+"\"}}";
            new PrivateJsonTask().execute("POST","GET_SALT","", "https://mad-hats.me/get_salt.json", input);
        }
    }

    private class PrivateJsonTask extends JsonTask {
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json_object = new JSONObject(result);
                if (json_object != null) {
                    switch (json_object.optString("call")){
                        case "LOGIN" :
                            jwt_token = json_object.optString("auth_token");
                            if(jwt_token !=""){
                                MainActivity.this.goToChatPage();
                            }
                            break;
                        case "GET_SALT" :
                            salt = json_object.optString("salt");
                            if(salt.compareTo("")!=0){
                                MainActivity.this.login();
                            }
                            break;
                        default :
                            System.out.println("Error ----------------------------------------");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onRegisterClick(View v){
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

    private void goToChatPage() {
        Intent intent = new Intent(this, ChatIndex.class);
        intent.putExtra(JWTTOKEN, jwt_token);
        startActivity(intent);
    }

    private void login() {
        EditText username = (EditText) findViewById(R.id.username_txt);
        EditText password = (EditText) findViewById(R.id.password_txt);
        byte[] salt_byte = Base64.decode(salt,1);
        String password_hash = SCryptUtil.scrypt(password.getText().toString(), salt_byte, 16384, 8, 1);
        String input = "{ \"commit\": \"Log in\",\"user\":{\"email\": \""+username.getText().toString().trim()+"\",\"password\": \""+password_hash+"\",\"remember_me\":\"0\",\"resource\": \"user\"}}";
        new PrivateJsonTask().execute("POST","LOGIN","", "https://mad-hats.me/auth_user.json", input);
    }

}

