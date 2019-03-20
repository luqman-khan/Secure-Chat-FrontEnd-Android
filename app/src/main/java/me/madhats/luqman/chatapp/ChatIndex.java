package me.madhats.luqman.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatIndex extends AppCompatActivity {
    public final static String CONTACT_USER = "com.example.chatapp.contact_user";
    String jwt_token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_index);
        Intent intent = getIntent();
        jwt_token = intent.getStringExtra(MainActivity.JWTTOKEN);
        new PrivateJsonTask().execute("GET","get_contacts",jwt_token, "https://mad-hats.me/contacts.json");
    }
    private class PrivateJsonTask extends JsonTask {
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json_object = new JSONObject(result);
                switch (json_object.optString("call")){
                    case "get_contacts" :
                        JSONArray arrJson= json_object.getJSONArray("contacts");
                        String[] contacts = new String[arrJson.length()];
                        for(int i=0;i<arrJson.length();i++)
                            contacts[i]=arrJson.getString(i);
                        ChatIndex.this.show_contacts(contacts);
                        break;
                    default :
                        System.out.println("Error ----------------------------------------");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void addContactClick(View v){
        Intent intent = new Intent(this, AddContact.class);
        intent.putExtra(MainActivity.JWTTOKEN, jwt_token);
        startActivity(intent);
    }

    private void show_contacts(final String contacts[]) {
        LinearLayout layout = (LinearLayout)findViewById(R.id.button_list);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Button tv[] = new Button[contacts.length];
        final Intent intent = new Intent(this, ChatPage.class);
        intent.putExtra(MainActivity.JWTTOKEN, jwt_token);
        for(int i=0;i<contacts.length;i++)
        {
            tv[i] = new Button(this);
            tv[i].setText(contacts[i]);
            tv[i].setGravity(Gravity.CENTER_HORIZONTAL);
            tv[i].setLayoutParams(lp);
            intent.putExtra(CONTACT_USER, contacts[i]);
            tv[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button)v;
                    String buttonText = b.getText().toString();
                    intent.putExtra(ChatIndex.CONTACT_USER, buttonText);
                    startActivity(intent);
                }
            });
            layout.addView(tv[i]);
        }
    }
}
