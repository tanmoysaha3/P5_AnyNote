package com.example.p5_anynote;

public class LabelModel {
    private String Label_Name;

    public LabelModel(){

    }

    public LabelModel(String label_Name){
        this.Label_Name=label_Name;
    }

    public String getLabel_Name() {
        return Label_Name;
    }

    public void setLabel_Name(String label_Name) {
        Label_Name = label_Name;
    }
}
