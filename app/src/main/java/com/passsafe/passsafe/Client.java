package com.passsafe.passsafe;

/**
 * Created by Menooker on 2017/9/29.
 */
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.StringWriter;
import java.io.PrintWriter;

public class Client {
    private static final String USER_AGENT = "Mozilla/5.0";
    static final String URL="http://pwdmanager.australiasoutheast.cloudapp.azure.com/pwdmanager/";

    //send POST to url, returns the response string
    static String sendPost(String url,String urlParameters) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }
        catch (Exception e)
        {

        }
        return null;
    }
    public static String sendGet(String url)  {
        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }
        catch (Exception e)
        {
            ShowError(e);
        }
        return null;

    }
    //print the error to the logcat
    static void ShowError(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        Log.d("PassSafe",sStackTrace);
    }

    //encode the string for URL
    static String encode(String str)
    {
        try {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch (Exception e)
        {
            return null;
        }
    }

}
