/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openbravo.pos.sales.restaurant;

/**
 *
 * @author windu purnomo
 */
public class TimeEntity {

    private String detik;
    private String menit;
    private String jam;
    private String jam2;

    public TimeEntity() {
    }

    public TimeEntity(String detik, String menit, String jam) {
        this.detik = detik;
        this.menit = menit;
        this.jam = jam;
        this.jam = jam2;
    }

    public String getDetik() {
        return detik;
    }

    public void setDetik(String detik) {
        this.detik = detik;
    }

    public String getJam() {
        return jam;
    }

    public void setJam(String jam) {
        this.jam = jam;
    }
    public void setJam2(String jam) {
        this.jam2 = jam2;
    }

    public String getMenit() {
        return menit;
    }

    public void setMenit(String menit) {
        this.menit = menit;
    }
}
