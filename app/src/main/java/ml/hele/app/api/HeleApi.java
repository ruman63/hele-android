package ml.hele.app.api;
/**
 * Created by rumman on 12/3/17.
 */

public final class HeleApi {
    public static final String HELE_HOST_API_URL= "http://hele.herokuapp.com/api";
    public static final String HELE_RETRIEVE_ALL_PLACES_URL = HELE_HOST_API_URL + "/places/refine";
    public static final String HELE_RETRIEVE_CATEGORIES_URL = HELE_HOST_API_URL + "/categories";

    public static final String PARAMETER_SEARCH =  "searchString";

    public static String getDetailsURL(int id) {
        return HELE_HOST_API_URL+"/places/"+id;
    }






}
