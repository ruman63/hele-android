package ml.hele.app;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rumman on 12/3/17.
 */

public class DestinationArrayAdapter extends ArrayAdapter<Destination> {
    List<Destination> list;
    Context context;
    int layoutResource;


    public DestinationArrayAdapter(Context context, int resource, List<Destination> objects) {
        super(context, resource, objects);
        this.context=context;
        list=objects;
        layoutResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(context).inflate(layoutResource, null);

        TextView idView = (TextView) convertView.findViewById(R.id.id);
        TextView nameView = (TextView) convertView.findViewById(R.id.name);
       // TextView locationView = (TextView) convertView.findViewById(R.id.location);
        TextView categoryView = (TextView) convertView.findViewById(R.id.category);
        ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.thumbnail);

        Destination destination = list.get(position);
        Log.d("Item: ", destination.toString());
        idView.setText(destination.getId()+"");
        nameView.setText(destination.getName());

        categoryView.setText(destination.getCategory());
        if(destination.getThumb() != null ) {
            thumbnailView.setImageBitmap(destination.getThumb());
            thumbnailView.setAlpha(0.75f);
        }
        return convertView;
    }

    public void setData(ArrayList<Destination> items){
        list=items;
    }
}
