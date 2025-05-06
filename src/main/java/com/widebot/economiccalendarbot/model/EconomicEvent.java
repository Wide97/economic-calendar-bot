package com.widebot.economiccalendarbot.model;

public class EconomicEvent {

    private String ora;
    private String valuta;
    private String paese;
    private int stelle;
    private String titolo;

    // Costruttore vuoto (necessario per lo scraping o deserializzazione)
    public EconomicEvent() {
    }

    // Costruttore con tutti i campi
    public EconomicEvent(String ora, String valuta, String paese, int stelle, String titolo) {
        this.ora = ora;
        this.valuta = valuta;
        this.paese = paese;
        this.stelle = stelle;
        this.titolo = titolo;
    }

    public String getOra() {
        return ora;
    }

    public void setOra(String ora) {
        this.ora = ora;
    }

    public String getValuta() {
        return valuta;
    }

    public void setValuta(String valuta) {
        this.valuta = valuta;
    }

    public String getPaese() {
        return paese;
    }

    public void setPaese(String paese) {
        this.paese = paese;
    }

    public int getStelle() {
        return stelle;
    }

    public void setStelle(int stelle) {
        this.stelle = stelle;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    @Override
    public String toString() {
        return "EconomicEvent{" +
                "ora='" + ora + '\'' +
                ", valuta='" + valuta + '\'' +
                ", paese='" + paese + '\'' +
                ", stelle=" + stelle +
                ", titolo='" + titolo + '\'' +
                '}';
    }
}
