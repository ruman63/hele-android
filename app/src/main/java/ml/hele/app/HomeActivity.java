package ml.hele.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import ml.hele.app.api.HeleApi;
import ml.hele.app.api.OnPostPreExecute;
import ml.hele.app.api.RetrieveList;

import static android.view.View.GONE;

public class HomeActivity extends AppCompatActivity implements OnPostPreExecute<HashMap<String, Object>>, View.OnClickListener , AdapterView.OnItemClickListener, TextWatcher{
    View progress;
    TextView progressText;
    ProgressBar progressBar;
    ListView destinationsList;
    RetrieveList fetchList;
    Button nextButton, prevButton;
    EditText searchField;
    MenuItem searchMenu, closeMenu;
    int lastPage, currentPage;
    ContentValues parameters;
    ArrayList<Destination> destinationArrayList;
    View listFooterView;
    DestinationArrayAdapter destinationAdapter;
    private LruCache<String, Bitmap> mMemoryCache;  //Memory Cache for bitmap loading in List

    public static boolean isConnected(Context context) {
        ConnectivityManager cMan = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo net = cMan.getActiveNetworkInfo();
        return net != null && net.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        parameters = new ContentValues();
        searchField = (EditText) toolbar.findViewById(R.id.search_field);
        searchField.addTextChangedListener(this);
        currentPage = lastPage = 1;
        progress = findViewById(R.id.loading_view);
        progressBar = (ProgressBar) progress.findViewById(R.id.indefiniteBar);
        progressText = (TextView) progress.findViewById(R.id.loading_text);
        fetchList = new RetrieveList(this, progressText);
        destinationArrayList = new ArrayList<>();
        destinationsList = (ListView) findViewById(R.id.placelist);
        listFooterView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.list_footer, null);
        destinationsList.addFooterView(listFooterView, null, false);

        // categoriesOptions = (Spinner) findViewById(R.id.category_selector);
        nextButton = (Button) listFooterView.findViewById(R.id.list_footer_next);
        prevButton = (Button) listFooterView.findViewById(R.id.list_footer_prev);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);


        //destinationAdapter = new DestinationArrayAdapter(this,R.layout.list_item, destinationArrayList);
        // categoriesAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, R.id.spinner_category_name, categoriesArrayList);

        //destinationsList.setAdapter(destinationAdapter);
        // categoriesOptions.setAdapter(categoriesAdapter);
        destinationsList.setOnItemClickListener(this);

        /**
         * Get max available VM memory, exceeding this amount will throw an OutOfMemory Exception.
         * Init Memory Cache with 1/8 th of available memory in Kilobytes.
         */

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
        fetch(parameters);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        searchMenu = menu.findItem(R.id.action_search);
        closeMenu = menu.findItem(R.id.action_close_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                searchField.setVisibility(View.VISIBLE);
                closeMenu.setVisible(true);
                searchMenu.setVisible(false);
                break;
            case R.id.action_close_search:
                if(!searchField.getText().toString().equals(""))
                    searchField.setText(null);
                searchField.setVisibility(GONE);
                searchMenu.setVisible(true);
                closeMenu.setVisible(false);
            case R.id.action_feedback:
                showFeedbackForm();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFeedbackForm() {
    }

    public void fetch(ContentValues... params) {
        if (!isConnected(this)) {
            showInternetNotAvailableDialog();
            Log.d("NetWork State", "Not Connected");
        } else {
            fetchList.cancel(true);
            fetchList = new RetrieveList(this, progressText);
            fetchList.execute(params);
        }
    }

    /**
     * Callback method for RetrieveList AsyncTask. It is triggered just  before executing the task
     */
    @Override
    public void onPreExecute() {
        progress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        destinationsList.setVisibility(GONE);
        progressText.setText(getString(R.string.connecting));

    }

    /**
     * Callback Method for RetrieveList AsyncTask. It is triggered just after execution of the task is completed
     *
     * @param result
     * @param errorMessage
     */
    @Override
    public void onPostExecute(HashMap<String, Object> result, String errorMessage) {
        if (errorMessage != null) {
            showErrorMessage(errorMessage);
        } else {
            updateDisplay(result);
        }
    }
    private void updateDisplay(HashMap<String, Object> result) {
        ArrayList<Destination> destinations = (ArrayList<Destination>) result.get(RetrieveList.DATA_KEY);
        ContentValues pageInfo = (ContentValues) result.get(RetrieveList.PAGE_INFO_KEY);
        currentPage = pageInfo.getAsInteger(RetrieveList.PAGE_CURRENT_KEY);
        lastPage = pageInfo.getAsInteger(RetrieveList.PAGE_LAST_KEY);
        if (currentPage == lastPage)
            nextButton.setVisibility(GONE);
        else
            nextButton.setVisibility(View.VISIBLE);
        if (currentPage == 1)
            prevButton.setVisibility(GONE);
        else
            prevButton.setVisibility(View.VISIBLE);
        Log.d("Content Loaded", "Content length: " + destinations.size());
        destinationArrayList = destinations;
        if (destinationArrayList.isEmpty()) {
            destinationsList.setVisibility(GONE);
            progress.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            progressText.setText(getString(R.string.norecord));
        } else {
            progress.setVisibility(GONE);
            destinationsList.setVisibility(View.VISIBLE);
            Log.d("List: ", destinationArrayList.toString());
            destinationAdapter = new DestinationArrayAdapter(HomeActivity.this, R.layout.list_item, destinationArrayList);
            Log.d("Adapter: ", destinationAdapter.toString());
            destinationsList.setAdapter(destinationAdapter);
        }
    }
    private void showErrorMessage(String errorMessage) {
        Log.d("Content Failed to Load", errorMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Error Occured")
                .setMessage("Error: " + errorMessage)
                .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                }).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after){ }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override
    public void afterTextChanged(Editable s) {
        String searchString = s.toString();
        parameters.put(HeleApi.PARAMETER_SEARCH, searchString);
        parameters.put(RetrieveList.PAGE_NUMBER, 1);
        fetch(parameters);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showDestination(view);
    }


    /**
     * Add Bitmap to LruCache with unique string key
     *
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * Get Bitmap from cache using unique string key
     *
     * @param key
     * @return bitmap
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void showInternetNotAvailableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection")
                .setMessage("Please connect to a Wifi network or Mobile data, and Retry")
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fetch(parameters);
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).show();
    }

    //stub for destination details. on clicking any destination
    private void showDestination(View view) {
        TextView idView;
        idView = (TextView) view.findViewById(R.id.id);
        String ID = idView.getText().toString();
        Intent launchDetailsActivity = new Intent(this, DetailsActivity.class);
        launchDetailsActivity.putExtra(Destination.DEST_ID, ID);
        startActivity(launchDetailsActivity);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.list_footer_next:
                loadNext(parameters);
                break;
            case R.id.list_footer_prev:
                loadPrev(parameters);
                break;
            default:
                return;
        }
        fetch(parameters);
    }

    public void loadNext(ContentValues values) {
        values.remove(RetrieveList.PAGE_NUMBER);
        values.put(RetrieveList.PAGE_NUMBER, currentPage + 1);
    }

    public void loadPrev(ContentValues values) {
        values.remove(RetrieveList.PAGE_NUMBER);
        values.put(RetrieveList.PAGE_NUMBER, currentPage - 1);
    }
}
