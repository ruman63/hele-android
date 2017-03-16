package ml.hele.app;

import android.graphics.BitmapFactory;
import android.util.Base64;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by rumman on 12/3/17.
 */

public class Destination {
    private Integer id;
    private String name;
    private String location;
    private String category;
    private URL linkThumb;


    public Destination(int id, String name, String location, String category, String link) throws MalformedURLException {
        this.id = id;
        this.name = name;
        this.location = location;
        this.category = category;
        this.linkThumb = new URL(link);
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

    public URL getLinkThumb() {
        return linkThumb;
    }

    public void setLinkThumb(URL linkThumb) {
        this.linkThumb = linkThumb;
    }

}
