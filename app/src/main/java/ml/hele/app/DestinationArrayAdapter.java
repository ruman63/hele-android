package ml.hele.app;

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
    Context context;
    int layoutResource;
    LayoutInflater inflater;



    public static class ViewHolder{
        TextView idView;
        TextView nameView;
        TextView categoryView;
        ImageView thumbnailView;
        URL imageURL;
        Bitmap image;
    }

    private class DownloadThumbnail extends AsyncTask<ViewHolder, Void, ViewHolder>{
        @Override
        protected void onPreExecute() {

        }

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
            if(viewHolder.image != null)
               viewHolder.thumbnailView.setImageBitmap(viewHolder.image);
        }
    }

    DestinationArrayAdapter(Context context, int resource, List<Destination> objects) {
        super(context, resource, objects);
        this.context=context;
        inflater = LayoutInflater.from(context);
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
            convertView.setTag(viewHolder);
        }

        viewHolder = (ViewHolder)convertView.getTag();

        Destination destination = list.get(position);
        viewHolder.idView.setText(String.valueOf(destination.getId()));
        viewHolder.nameView.setText(destination.getName());
        viewHolder.categoryView.setText(destination.getCategory());
        viewHolder.imageURL = destination.getLinkThumb();
        viewHolder.thumbnailView.setImageResource(R.drawable.image_loading);
        new DownloadThumbnail().execute(viewHolder);

        return convertView;
    }


}
