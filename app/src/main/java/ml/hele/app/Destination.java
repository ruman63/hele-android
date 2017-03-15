package ml.hele.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by rumman on 12/3/17.
 */

public class Destination {
    private Integer id;
    private String name;
    private String location;
    private String category;
    private Bitmap thumb;


    public Destination(int id, String name, String location, String category, String encThumb) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.category = category;
        setThumb(encThumb);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public void setThumb( String base64EncodedImage) {
            byte[] decodedBytes = Base64.decode(base64EncodedImage, Base64.DEFAULT);
            thumb = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
