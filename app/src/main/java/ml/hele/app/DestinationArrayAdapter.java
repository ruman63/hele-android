package ml.hele.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import ml.hele.app.api.OnPostPreExecute;

/**
 * Created by rumman on 12/3/17.
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
        URL imageURL;
        String cacheKey;
        Bitmap image;
    }

    private class DownloadThumbnail extends AsyncTask<ViewHolder, Void, ViewHolder>{

        @Override
        protected ViewHolder doInBackground(ViewHolder... params) {
            ViewHolder viewHolder  =params[0];
            try{
                URL url = viewHolder.imageURL;
                Log.d("URL: ", url.toString());
                viewHolder.image = BitmapFactory.decodeStream(url.openStream());
            } catch (IOException e){
                Log.d("Error Loading Thumbnail", e.getMessage());
                viewHolder.image = null;
            }

            return viewHolder;
        }

        @Override
        protected void onPostExecute(ViewHolder viewHolder) {

            /**
             * if downmloaded successfully add to memcache
             */

            if(viewHolder.image != null) {
                caller.addBitmapToMemoryCache(viewHolder.cacheKey, viewHolder.image);
                viewHolder.thumbnailView.setImageBitmap(viewHolder.image);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView==null) {
            convertView = inflater.inflate(layoutResource, null);
            viewHolder = new ViewHolder();
            viewHolder.idView = (TextView) convertView.findViewById(R.id.id);
            viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
            viewHolder.categoryView = (TextView) convertView.findViewById(R.id.category);
            viewHolder.thumbnailView = (ImageView) convertView.findViewById(R.id.thumbnail);
            viewHolder.cacheKey = "";
            convertView.setTag(viewHolder);
        }

        viewHolder = (ViewHolder)convertView.getTag();

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

        viewHolder.cacheKey = destination.getId()+"_"+destination.getName();
        final Bitmap bitmap = caller.getBitmapFromMemCache(viewHolder.cacheKey);

        if(bitmap != null){
            viewHolder.thumbnailView.setImageBitmap(bitmap);
        }

        else {
            //else if bitmap not available in cache then, download

            viewHolder.thumbnailView.setImageResource(R.drawable.image_loading);
            new DownloadThumbnail().execute(viewHolder);
        }

        return convertView;
    }


}
