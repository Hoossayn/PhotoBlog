package com.example.android.photoblog;


import java.sql.Timestamp;
import java.util.Date;

public class BlogPost {

    public String image_url, user_id, desc;
    public Date timeStamp;

    public BlogPost(){}

    public BlogPost(String image_url, String user_id, String desc,Date timeStamp) {
        this.image_url = image_url;
        this.user_id = user_id;
        this.desc = desc;
        this.timeStamp = timeStamp;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
