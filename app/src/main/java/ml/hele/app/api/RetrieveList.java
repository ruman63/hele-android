package ml.hele.app.api;

import android.os.AsyncTask;

import ml.hele.app.Destination;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by rumman on 12/3/17.
 */

public class RetrieveList extends AsyncTask< String, Void, ArrayList<Destination> > {

    OnPostPreExecute < ArrayList <Destination> > beforeAndAfter = null;

    String error = null;

    public RetrieveList(OnPostPreExecute < ArrayList <Destination> > beforeAndAfter ){

        this.beforeAndAfter = beforeAndAfter;
    }

    @Override
    protected void onPreExecute() {
        beforeAndAfter.onPreExecute();
    }

    @Override
    protected ArrayList<Destination> doInBackground(String... params)  {
        ArrayList<Destination> allList= new ArrayList<Destination>();
        try {
            String addr = HeleApi.HELE_RETRIEVE_ALL_PLACES_URL;
            URL urls = new URL(addr);
            HttpURLConnection conn = (HttpURLConnection) urls.openConnection();
            conn.setReadTimeout(30000); //milliseconds
            conn.setConnectTimeout(30000); // milliseconds
            conn.setRequestMethod("GET");
          //  OutputStream os = conn.getOutputStream();

            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String json = null;
                while ((json = reader.readLine()) != null) {
                    sb.append(json);

                }
                JSONArray array = new JSONArray(sb.toString());
                for(int i=0; i<array.length();i++){

                    JSONObject object=array.getJSONObject(i);
                    int id = object.getInt("id");

                    String name = object.getString("name");
                    String location = object.getString("location");
                    String category = object.getString("category");
                    String linkthumb = object.getString("thumb");

                    Destination destination = new Destination(id, name, location, category, linkthumb);
                    allList.add(destination);

                }

            }
            else {
                error = "Connection Staus: "+conn.getResponseCode()+": "+conn.getResponseMessage();
            }


        } catch (Exception e) {
            error = e.getMessage();
        }

        return allList;
    }

    @Override
    protected void onPostExecute(ArrayList<Destination> list){
        beforeAndAfter.onPostExecute(list, error);
    }
}
