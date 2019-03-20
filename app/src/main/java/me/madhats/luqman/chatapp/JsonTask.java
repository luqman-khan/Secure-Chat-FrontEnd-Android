package me.madhats.luqman.chatapp;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonTask extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... params) {
        System.out.println("Inside json method call ****************************************************");
        HttpURLConnection urlConnection = null;
        URL url = null;
        InputStream inStream = null;
        try {
            url = new URL(params[3]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(params[0]);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            System.out.println(params[2] + " Old token ............................88888888888888888");
            if (params[2] != "") {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1"+params[2]);
                urlConnection.setRequestProperty("Authorization", params[2]);
            }
            if (params[0].compareTo("POST") == 0 || params[0].compareTo("PUT") == 0 ) {
                String input = params[4];
                OutputStream os = urlConnection.getOutputStream();
                os.write(input.getBytes());
                os.flush();
            }
            urlConnection.connect();
            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }
            temp = response.split("^\\{")[1];
            response = "{\"call\":\""+params[1].toString()+"\",";
            response += temp;
            System.out.println(response);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(e + "Inside error");
        }
        return null;
    }
}
