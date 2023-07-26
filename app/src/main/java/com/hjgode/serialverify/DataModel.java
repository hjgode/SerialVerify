package com.hjgode.serialverify;

public class DataModel {

    String ID;
    String Serial;
    String Model;
    String Bezeichnung;
    String Auftrag;
    String Bemerkung;

    public DataModel(String id, String s, String m, String bez,  String a, String bem) {
        this.ID=id;
        this.Serial=s;
        this.Model=m;
        this.Bezeichnung=bez;
        this.Auftrag=a;
        this.Bemerkung=bem;
   }

    public String getSerial() {
        return Serial;
    }

    public String getModel() {
        return Model;
    }
    public String getBezeichnung() {
        return Bezeichnung;
    }
    public String getAuftrag() {
        return Auftrag;
    }
    public String getBemerkung() {
        return Bemerkung;
    }

    public String getID() {
        return ID;
    }
}