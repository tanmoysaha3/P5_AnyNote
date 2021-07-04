package com.example.p5_anynote;

import com.google.firebase.Timestamp;

public class NoteModel {
    private Timestamp Date;
    private String Title;
    private String Content;
    private String Label;
    private String Important;

    public NoteModel(){

    }

    public NoteModel(Timestamp date, String title, String content, String label, String important){
        this.Date=date;
        this.Title=title;
        this.Content=content;
        this.Label=label;
        this.Important=important;
    }

    public Timestamp getDate() {
        return Date;
    }

    public void setDate(Timestamp date) {
        Date = date;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getLabel() {
        return Label;
    }

    public void setLabel(String label) {
        Label = label;
    }

    public String getImportant() {
        return Important;
    }

    public void setImportant(String important) {
        Important = important;
    }
}
