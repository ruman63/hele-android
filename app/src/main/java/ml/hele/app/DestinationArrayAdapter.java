package ml.hele.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import ml.hele.app.api.OnPostPreExecute;

/**
 * Created by rumman on 12/3/17.
 * Corresponds to Module
 */

public class DestinationArrayAdapter extends ArrayAdapter<Destination> {
    List<Destination> list;
    int layoutResource;
    LayoutInflater inflater;
    HomeActivity caller ;


    public static class ViewHolder{
        TextView idView;
        TextView nameView;
        TextView categoryView;
        ImageView thumbnailView;
        ProgressBar progress;
        URL imageURL;
        Bitmap image;
    }

    private class DownloadThumbnail extends AsyncTask<ViewHolder, Void, ViewHolder>{

        @Override
        protected ViewHolder doInBackground(ViewHolder... params) {
            ViewHolder view = params[0];
            view.image = null;
            try{
                URL url= view.imageURL;
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(25000);
                conn.connect();
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    view.image = BitmapFactory.decodeStream(conn.getInputStream());
                }
                else {
                    view.image = null;
                    Log.d("Thumbnail", "Unable to Connect Server sent Response code: "+conn.getResponseCode());
                }
            } catch (Exception e){
                Log.d("Thumbnail", e.getMessage());
                view.image = null;
            }

            return view;
        }

        @Override
        protected void onPostExecute(ViewHolder view) {

            /**
             * if downmloaded successfully add to memcache
             */
            Log.d("Thumbnail", view.nameView.getText()+" - Downloaded bitmap");
            view.progress.setVisibility(View.GONE);

            if(view.image != null) {
                caller.addBitmapToMemoryCache(view.imageURL.toString(), view.image);
                view.thumbnailView.setImageBitmap(view.image);
            }
            else {
                view.thumbnailView.setImageResource(R.drawable.loading);
            }
        }
    }

    DestinationArrayAdapter(Context context, int resource, List<Destination> objects) {
        super(context, resource, objects);
        caller= (HomeActivity)(context);
        inflater = LayoutInflater.from(caller);
        list=objects;
        layoutResource = resource;
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView==null) {
            convertView = inflater.inflate(layoutResource, null);
            viewHolder = new ViewHolder();
            viewHolder.idView = (TextView) convertView.findViewById(R.id.id);
            viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
            viewHolder.categoryView = (TextView) convertView.findViewById(R.id.category);
            viewHolder.progress = (ProgressBar) convertView.findViewById(R.id.load);
            viewHolder.thumbnailView = (ImageView) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(viewHolder);
        }

        viewHolder = (ViewHolder)convertView.getTag();
        viewHolder.thumbnailView.setImageDrawable(null);
        viewHolder.progress.setVisibility(View.VISIBLE);
        Destination destination = list.get(position);
        viewHolder.idView.setText(String.valueOf(destination.getId()));
        viewHolder.nameView.setText(destination.getName());
        viewHolder.categoryView.setText(destination.getCategory());
        viewHolder.imageURL = destination.getLinkThumb();

        /**
         * Assign a Unique Key i.e UID_Name for cache
         * Get instance of caller activity that implements cache
         * Get Bitmap from Mem cache
         * If bitmap available set it to thumbnail
         */
        Log.d("Thumbnail", "Looking into cache for "+destination.getName()+destination.getLinkThumb().toString());

        final Bitmap bitmap = caller.getBitmapFromMemCache(destination.getLinkThumb().toString());

        if(bitmap != null){
            Log.d("Thumbnail", viewHolder.nameView.getText()+" Found in Cache");
            viewHolder.image = bitmap;
            viewHolder.thumbnailView.setImageBitmap(viewHolder.image);
            viewHolder.progress.setVisibility(View.GONE);
        }

        else {
            //else if bitmap not available in cache then, download
            Log.d("Thumbnail", viewHolder.nameView.getText()+" Not found in cache, downloading");
            viewHolder.thumbnailView.setImageDrawable(null);
            viewHolder.progress.setVisibility(View.VISIBLE);
            new DownloadThumbnail().execute(viewHolder);
        }

        return convertView;
    }


}
