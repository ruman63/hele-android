package ml.hele.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.net.HttpURLConnection;
import java.net.URL;

import ml.hele.app.api.OnPostPreExecute;
import ml.hele.app.api.RetrieveDetails;
import ml.hele.app.api.RetrieveList;

public class DetailsActivity extends AppCompatActivity implements OnPostPreExecute<Bundle>{
    Integer destID;
    RetrieveDetails fetchDetailsTask;
    CarouselView photosCarousel;
    TextView nameView, locationView, cityView, descView;
    ProgressBar mainProgress;
    ProgressBar photosProgress;
    String[] photosURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        destID = Integer.parseInt(getIntent().getStringExtra(Destination.DEST_ID));
        photosCarousel = (CarouselView) findViewById(R.id.photos);
        nameView = (TextView) findViewById(R.id.name);
        locationView = (TextView) findViewById(R.id.location);
        cityView = (TextView) findViewById(R.id.city);
        descView = (TextView) findViewById(R.id.description);
        mainProgress = (ProgressBar) findViewById(R.id.details_progress);
        photosProgress = (ProgressBar) findViewById(R.id.details_photo_progress);
        startFetchDetailsTask(destID);
    }
    private void startFetchDetailsTask(Integer id){
        fetchDetailsTask = new RetrieveDetails(this);
        fetchDetailsTask.execute(id);
    }

    @Override
    public void onPreExecute() {
        Log.d("RetrieveDetails", "Starting Execution");
        mainProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPostExecute(Bundle values, String errorMessage) {
        mainProgress.setVisibility(View.GONE);
        Log.d("RetrieveDetails", "ExecutionCompleted");
        if(errorMessage != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
            builder.setTitle("Error Fetching Details")
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startFetchDetailsTask(destID);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
        else {
            String name = values.getString(Destination.DEST_NAME);
            String location = values.getString(Destination.DEST_LOCATION);
            String city = values.getString("city");
            String desc = values.getString(Destination.DEST_DESC);

            nameView.setText(name);
            locationView.setText(location);
            cityView.setText(city);
            descView.setText(desc);
            photosURL = values.getStringArray("photos");
            photosCarousel.setImageListener(new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {
                    new DownloadPhotosTask(imageView).execute(photosURL[position]);
                }
            });
            photosCarousel.setPageCount(photosURL.length);

        }
    }

    class DownloadPhotosTask extends AsyncTask<String, Void, Bitmap>{
        ImageView imageView;
        String errorMessage;
        String paramUrl;
        DownloadPhotosTask(ImageView imageView){
            this.imageView  = imageView;
        }

        @Override
        protected void onPreExecute() {
            photosProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            paramUrl = params[0];
            try {
                URL url= new URL(paramUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    photo = BitmapFactory.decodeStream(conn.getInputStream());
                }
                else if( conn.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT  ) {
                    errorMessage = "There seems to be a problem with your internet connection.";
                }
                else {
                    Log.d("Connection Error", conn.getResponseMessage());
                    errorMessage = conn.getResponseMessage();
                }
            } catch(Exception e) {
                errorMessage=e.getMessage();
            }

            return photo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                builder.setTitle("Error Fetching Details")
                        .setMessage(errorMessage)
                        .setCancelable(false)
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }
            photosProgress.setVisibility(View.GONE);
            imageView.setImageBitmap(bitmap);
        }
    }
}
