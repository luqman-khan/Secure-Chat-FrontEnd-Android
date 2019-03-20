package me.madhats.luqman.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;

import com.lambdaworks.crypto.SCryptUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void onRegisterClick(View v) throws Exception{
        EditText username = (EditText) findViewById(R.id.email_txt);
        EditText password = (EditText) findViewById(R.id.password_txt);
        byte[] salt = new byte[16];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
        String salt_string = Base64.encodeToString(salt,1).replace("\n", "").replace("\r", "");
        String password_hash = SCryptUtil.scrypt(password.getText().toString(), salt, 16384, 8, 1).replace("\n", "").replace("\r", "");
        String input = "{ \"commit\": \"Log in\",\"user\":{\"email\": \""+username.getText().toString().trim()+"\",\"password\": \""+password_hash+"\",\"remember_me\":\"0\",\"resource\": \"user\",\"salt\":\""+salt_string+"\"}}";
        new Register.PrivateJsonTask().execute("POST","REGISTER","", "https://mad-hats.me/users.json", input);
    }

    private class PrivateJsonTask extends JsonTask {
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json_object = new JSONObject(result);
                switch (json_object.optString("call")){
                    case "REGISTER" :
                        AlertDialog.Builder alert_message = new AlertDialog.Builder(getApplicationContext());
                        alert_message.setMessage(json_object.optString("notice"))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which){
                                        Intent intent = new Intent(getApplicationContext(), Register.class);
                                        startActivity(intent);
                                    }
                                })
                                .create();
                        alert_message.show();
                        break;
                    default :
                        System.out.println("Error ----------------------------------------");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
