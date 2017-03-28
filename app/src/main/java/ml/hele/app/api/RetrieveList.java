package ml.hele.app.api;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import ml.hele.app.Destination;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by rumman on 12/3/17.
 */

public class RetrieveList extends AsyncTask < ContentValues, String, HashMap <String, Object> > {

    OnPostPreExecute < HashMap <String, Object> > beforeAndAfter = null;
    public static final String PAGE_NUMBER = "page";
    public static final String PAGE_CURRENT_KEY = "current";
    public static final String PAGE_LAST_KEY = "last";
    public static final String PAGE_INFO_KEY = "meta";
    public static final String DATA_KEY = "data";
    private String error;
    private TextView progressText;


    public RetrieveList(OnPostPreExecute < HashMap <String, Object> > beforeAndAfter , TextView text){
        this.beforeAndAfter = beforeAndAfter;
        progressText = text;
        error = null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        progressText.setText(values[0]);
    }

    @Override
    protected void onPreExecute() {
        beforeAndAfter.onPreExecute();
    }

    /**
     *
     * @param params takes ContentValues as ONE argument with specific key "PAGE" for pageNumber
     * @return returns hashmap with two keys -
     * 1) PAGE_INFO_KEY corresponds to a ContentValues Object containing current and last page
     * 2) DATA_KEY corresponds to a ArrayList of Destinations
     */
    @Override
    protected HashMap <String, Object> doInBackground(ContentValues... params)  {
        HashMap<String, Object> result = new HashMap<String, Object>();
        ArrayList<Destination> allList= new ArrayList<Destination>();
        try {
            String addr = HeleApi.HELE_RETRIEVE_ALL_PLACES_URL;
            if (params.length > 0 && params[0].containsKey(PAGE_NUMBER)){
                addr+="?page="+params[0].get(PAGE_NUMBER);
            }
            URL urls = new URL(addr);
            HttpURLConnection conn = (HttpURLConnection) urls.openConnection();
            conn.setReadTimeout(30000); // milliseconds
            conn.setConnectTimeout(30000); // milliseconds
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));

            if(params.length > 0){
                writer.write(getQuery(params[0]));
                writer.flush();
            }

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
                JSONObject pagination = jsonData.getJSONObject(PAGE_INFO_KEY);
                ContentValues paginationInfo = new ContentValues();
                paginationInfo.put(PAGE_CURRENT_KEY,pagination.getString(PAGE_CURRENT_KEY));
                paginationInfo.put(PAGE_LAST_KEY, pagination.getString(PAGE_LAST_KEY));
                JSONArray array = jsonData.getJSONArray(DATA_KEY);
                for(int i=0; i<array.length();i++){

                    JSONObject object=array.getJSONObject(i);

                    int id = object.getInt(Destination.DEST_ID);
                    String name = object.getString(Destination.DEST_NAME);
                    String location = object.getString(Destination.DEST_LOCATION);
                    String category = object.getString(Destination.DEST_CATEGORY);
                    String linkthumb = object.getString(Destination.DEST_THUMBNAIL);

                    Destination destination = new Destination(id, name, location, category, linkthumb);
                    allList.add(destination);

                }
                result.put(PAGE_INFO_KEY, paginationInfo);
                result.put(DATA_KEY, allList);
            }
            else {
                error = "Connection Staus: "+conn.getResponseCode()+": "+conn.getResponseMessage();
                result = null;
            }

            writer.close();
        } catch (Exception e) {
            error = e.getMessage();
        }

        return result;
    }

    /**
     *
     * @param result
     */
    @Override
    protected void onPostExecute(HashMap<String, Object> result){
        beforeAndAfter.onPostExecute(result, error);
    }

    private String getQuery(ContentValues values) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : values.valueSet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }


        return result.toString();
    }
}
