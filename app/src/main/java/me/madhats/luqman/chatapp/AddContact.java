package me.madhats.luqman.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class AddContact extends AppCompatActivity {

    String jwt_token, contact_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        Intent intent = getIntent();
        jwt_token = intent.getStringExtra(MainActivity.JWTTOKEN);
        contact_user = intent.getStringExtra(ChatIndex.CONTACT_USER);
    }

    public void onAddClick(View v){
        EditText email = (EditText) findViewById(R.id.email_txt);
        String input = "{ \"commit\": \"Add Contact\",\"contact\":{\"contact_user\": \""+email.getText().toString().trim()+"\"}}";
        new AddContact.PrivateJsonTask().execute("POST","ADD_CONTACT",jwt_token, "https://mad-hats.me/contacts.json", input);
    }

    private class PrivateJsonTask extends JsonTask {
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json_object = new JSONObject(result);
                switch (json_object.optString("call")){
                    case "ADD_CONTACT" :
                        String errors = json_object.optString("errors");
                        if(errors.compareTo("")==0) {
                            Intent intent = new Intent(getApplicationContext(), ChatIndex.class);
                            intent.putExtra(MainActivity.JWTTOKEN, jwt_token);
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder alert_message = new AlertDialog.Builder(getApplicationContext());
                            alert_message.setMessage(errors)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which){
                                        }
                                    })
                                    .create();
                            alert_message.show();
                        }
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
