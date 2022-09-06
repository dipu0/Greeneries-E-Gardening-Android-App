package com.chowdhuryelab.greeneries.models;

public class ModelBlog {
    private String blogId, blogTitle, blogDescription, prductCategory,  blogImg, timestamp, uid;

    public ModelBlog() {
    }

    public String getBlogId() {
        return blogId;
    }

    public void setBlogId(String blogId) {
        this.blogId = blogId;
    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

    public String getBlogDescription() {
        return blogDescription;
    }

    public void setBlogDescription(String blogDescription) {
        this.blogDescription = blogDescription;
    }

    public String getPrductCategory() {
        return prductCategory;
    }

    public void setPrductCategory(String prductCategory) {
        this.prductCategory = prductCategory;
    }

    public String getblogImg() {
        return blogImg;
    }

    public void setblogImg(String blogImg) {
        this.blogImg = blogImg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ModelBlog(String blogId, String blogTitle, String blogDescription, String prductCategory,
                     String blogImg, String timestamp, String uid) {
        this.blogId = blogId;
        this.blogTitle = blogTitle;
        this.blogDescription = blogDescription;
        this.prductCategory = prductCategory;
        this.blogImg = blogImg;
        this.timestamp = timestamp;
        this.uid = uid;


    }
}
