package ml.hele.app.api;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ml.hele.app.Destination;

/**
 * Created by rumman on 4/4/17.
 */

public class RetrieveDetails extends AsyncTask<Integer, String, Bundle> {
    private OnPostPreExecute<Bundle> callback;
    private String error;
    public  RetrieveDetails( OnPostPreExecute<Bundle> callback ){
        this.callback = callback;
    }
    @Override
    protected Bundle doInBackground(Integer[] params) {

        Bundle result = new Bundle();
        try {
            URL url = new URL(HeleApi.getDetailsURL(params[0]));
            HttpURLConnection conn  = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.connect();
            publishProgress("Loading Destinations ...");
            Log.d("Connection", "Acccepted");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String json = null;
                while ((json = reader.readLine()) != null) {
                    sb.append(json);

                }
                JSONObject jsonData = new JSONObject(sb.toString());
                result.putInt( Destination.DEST_ID, jsonData.getInt(Destination.DEST_ID));
                result.putString( Destination.DEST_NAME, jsonData.getString(Destination.DEST_NAME));
                result.putString( Destination.DEST_LOCATION, jsonData.getString(Destination.DEST_LOCATION));
                result.putString("city", jsonData.getString("city"));
                result.putString(Destination.DEST_DESC, jsonData.getString(Destination.DEST_DESC));
                result.putDouble("ratingAvg", jsonData.getDouble("ratingAvg"));
                JSONArray photos = jsonData.getJSONArray("photos");
                String photosURLs[] = new String [photos.length()];
                for(int i=0;i<photos.length();i++){
                    photosURLs[i] = photos.getString(i);
                }
                result.putStringArray("photos", photosURLs);
            }
            else if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR){
                error = "Error ("+conn.getResponseCode()+"): Internal Server Error Occured";
            }
            else {
                error  ="Error ("+conn.getResponseCode()+"): "+conn.getResponseMessage();
            }
        } catch (Exception e){
            result = null;
            error = e.getMessage();
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        callback.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bundle values) {
        callback.onPostExecute(values, error);
    }
}
