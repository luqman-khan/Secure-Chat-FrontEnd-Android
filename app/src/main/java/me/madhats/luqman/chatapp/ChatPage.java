package me.madhats.luqman.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatPage extends AppCompatActivity {

    public final static String CONTACT_USER = "com.example.chatapp.contact_user";
    String jwt_token, contact_user, current_user;
    DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);
        Intent intent = getIntent();
        jwt_token = intent.getStringExtra(MainActivity.JWTTOKEN);
        contact_user = intent.getStringExtra(ChatIndex.CONTACT_USER);
        String input = "{ \"commit\": \"Messages\",\"message\":{\"to\": \""+contact_user+"\"}}";
        new PrivateJsonTask().execute("PUT", "get_messages",jwt_token, "https://mad-hats.me/messages.json", input);
    }
    public void sendClick(View v) throws Exception {
        EditText typed_message = (EditText) findViewById(R.id.typed_message);
        System.out.println("Contact user is ........1"+contact_user);
        if(typed_message.getText().toString().trim().compareTo("")==0) {
            String input = "{ \"commit\": \"Messages\",\"message\":{\"to\": \""+contact_user+"\"}}";
            new PrivateJsonTask().execute("PUT", "get_messages",jwt_token, "https://mad-hats.me/messages.json", input);
        } else {
            db = new DatabaseHelper(this);
            Cursor key = db.getData(contact_user);
            String secure_key ="";
            key.moveToFirst();
            while (!key.isAfterLast()) {
                secure_key = key.getString(key.getColumnIndex("KEY"));
                key.moveToNext();
            }
            if(secure_key.compareTo("")!=0) {
                System.out.print("Key At encryption = "+ secure_key);
                AESEncryption aes_enc = new AESEncryption(secure_key);
                String message = aes_enc.encrypt(typed_message.getText().toString()).replace("\n", "").replace("\r", "");
                String input = "{ \"commit\": \"Messages\",\"message\":{\"to\": \"" + contact_user + "\",\"message\":\"" + message + "\"}}";
                new PrivateJsonTask().execute("POST", "send_message", jwt_token, "https://mad-hats.me/messages.json", input);
            } else {
                AlertDialog.Builder alert_message = new AlertDialog.Builder(this);
                alert_message.setMessage("Please set a key for Encryption")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                System.out.println("Contact user is ........2"+contact_user);
                                Intent intent = new Intent(getApplicationContext(), GenerateKey.class);
                                intent.putExtra(ChatPage.CONTACT_USER, contact_user);
                                startActivity(intent);
                            }
                        })
                        .create();
                alert_message.show();
            }
        }
    }

    public void newKeyClick(View v){
        Intent intent = new Intent(this, GenerateKey.class);
        intent.putExtra(ChatPage.CONTACT_USER, contact_user);
        startActivity(intent);
    }

    private class PrivateJsonTask extends JsonTask {

        @Override
        protected void onPostExecute(String result) {
            EditText typed_message = (EditText) findViewById(R.id.typed_message);
            try {
                JSONObject json_object = new JSONObject(result);
                switch (json_object.optString("call")){
                    case "get_messages" :
                        current_user = json_object.optString("current_user");
                        JSONArray arrJson= json_object.getJSONArray("messages");
                        String[][] messages = new String[arrJson.length()][3];
                        for(int i=0;i<arrJson.length();i++) {
                            JSONObject message_object = new JSONObject(arrJson.getString(i));
                            messages[i][0] = message_object.optString("message");
                            messages[i][1] = message_object.optString("from");
                            messages[i][2] = message_object.optString("created_at");
                        }
                        typed_message.setText("");
                        ChatPage.this.display_messages(messages);
                        break;
                    case "send_message" :
                        typed_message.setText("");
                        String input = "{ \"commit\": \"Messages\",\"message\":{\"to\": \""+contact_user+"\"}}";
                        new PrivateJsonTask().execute("PUT", "get_messages",jwt_token, "https://mad-hats.me/messages.json", input);
                        break;
                    default :
                        System.out.println("Error ----------------------------------------");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void display_messages(String[][] messages) throws Exception {

        LinearLayout message_list = (LinearLayout) findViewById(R.id.message_list);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        db = new DatabaseHelper(this);
        Cursor key = db.getData(contact_user);
        String secure_key ="";
        key.moveToFirst();
        while (!key.isAfterLast()) {
            secure_key = key.getString(key.getColumnIndex("KEY"));
            key.moveToNext();
        }
        System.out.print("Key At decryption = "+ secure_key);
        AESEncryption aes_enc = new AESEncryption(secure_key);
        lp.setMargins(10,10,20,20);
        TextView tv[] = new TextView[messages.length];
        final Intent intent = new Intent(this, ChatPage.class);
        intent.putExtra(MainActivity.JWTTOKEN, jwt_token);
        message_list.removeAllViews();
        for(int i=0;i<messages.length;i++)
        {
            tv[i] = new TextView(this);
            tv[i].setText(aes_enc.decrypt(messages[i][0]));
            tv[i].setTextSize(30);
            tv[i].setBackgroundColor(0xffcccccc);
            tv[i].setGravity(Gravity.RIGHT);
            tv[i].setLayoutParams(lp);
            message_list.addView(tv[i]);
        }
    }
}
