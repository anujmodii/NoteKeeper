package com.example.notekeeper.Model;

public class Notes {
    private String noteid;
    private String title;
    private String note;
    private String color;
    private String priority;
    private String importance;
    private String date;
    private String imageURL;


    public Notes(String noteid, String title, String note, String color, String priority, String importance, String date,String imageURL) {
        this.noteid = noteid;
        this.title = title;
        this.note = note;
        this.color = color;
        this.priority = priority;
        this.importance = importance;
        this.date = date;
        this.imageURL = imageURL;

    }

    public Notes() {
    }

    public String getnoteid() {
        return noteid;
    }

    public void setnoteid(String noteid) {
        this.noteid = noteid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
