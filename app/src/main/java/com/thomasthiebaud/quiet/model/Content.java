package com.thomasthiebaud.quiet.model;

/**
 * Created by thomasthiebaud on 5/11/16.
 */
public class Content {
    private String number;
    private int scam;
    private int ad;
    private int score;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getScam() {
        return scam;
    }

    public void setScam(int scam) {
        this.scam = scam;
    }

    public int getAd() {
        return ad;
    }

    public void setAd(int ad) {
        this.ad = ad;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
