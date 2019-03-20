package me.madhats.luqman.chatapp;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class GenerateKey extends AppCompatActivity {

    DatabaseHelper db ;
    String contact_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_key);
        Intent intent = getIntent();
        contact_user = intent.getStringExtra(ChatPage.CONTACT_USER);
        EditText key_txt = (EditText) findViewById(R.id.key_txt);
        db = new DatabaseHelper(this);
        Cursor key = db.getData(contact_user);
        System.out.println("INSIDE GENERATE KEY ONCREATE......................................................."+ key);
        key.moveToFirst();
        while (!key.isAfterLast()) {
            key_txt.setText(key.getString(key.getColumnIndex("KEY")));
            key.moveToNext();
        }
    }

    public void getKeyClick(View v) throws Exception{
        EditText key_seed_txt = (EditText) findViewById(R.id.key_seed_txt);
        db = new DatabaseHelper(this);
        byte [] salt = new byte[32];
        String algorithm = "PBKDF2WithHmacSHA1";
        int derivedKeyLength = 192;
        int iterations = 20000;
        System.out.println("KEY SEED IS "+key_seed_txt.getText().toString()+"......................................................."+(key_seed_txt.getText().toString().compareTo("")!=0));
        if(key_seed_txt.getText().toString().compareTo("")!=0){
            new SecureRandom().nextBytes(salt);
            KeySpec spec = new PBEKeySpec(key_seed_txt.getText().toString().toCharArray(), salt , iterations, derivedKeyLength);
            SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
            String secret_key = Base64.encodeToString(f.generateSecret(spec).getEncoded(),1).replace("\n", "").replace("\r", "");
            System.out.println("INSIDE GETKEYCLICK......................................................."+secret_key.length());
            if(secret_key.length()==32){
                db.insertData(contact_user,secret_key);
            }
            key_seed_txt.setText("");
        }
        EditText key_txt = (EditText) findViewById(R.id.key_txt);
        Cursor key = db.getData(contact_user);
        key.moveToFirst();
        while (!key.isAfterLast()) {
            key_txt.setText(key.getString(key.getColumnIndex("KEY")));
            key.moveToNext();
        }
    }

    public void saveKeyClick(View v){
        EditText user_hash_key_txt = (EditText) findViewById(R.id.user_hash_key_txt);
        if(user_hash_key_txt.getText().toString().length()==32){
            db.insertData(contact_user,user_hash_key_txt.getText().toString());
            EditText key_txt = (EditText) findViewById(R.id.key_txt);
            Cursor key = db.getData(contact_user);
            key.moveToFirst();
            while (!key.isAfterLast()) {
                key_txt.setText(key.getString(key.getColumnIndex("KEY")));
                key.moveToNext();
            }
        }


    }
}
