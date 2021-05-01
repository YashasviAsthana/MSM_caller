package com.projects.mara.msm_caller;

import android.graphics.Bitmap;

/**
 * Created by Yashasvi on 02-06-2017.
 */

public class contact {

    String name;
    String phone;
    String id;
    Bitmap thumb;

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setId(String id) {
        this.id = id;
    }
}
