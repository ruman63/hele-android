package ml.hele.app.api;

/**
 * Created by rumman on 12/3/17.
 */

public interface OnPostPreExecute<Result> {
    public void onPreExecute();
    public void onPostExecute(Result result, String errorMessage);
}
