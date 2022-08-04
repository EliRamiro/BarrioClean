package com.aply.barriocleandriver.utils;

public class AppSettings {
    String comision;
    String wait_time;

    public AppSettings() {
    }

    public double getComision() {
        double d_comision = Double.parseDouble(comision) / 100f;
        return d_comision;
    }

    public void setComision(String comision) {
        this.comision = comision;
    }

    public String getWait_time() {
        return wait_time;
    }

    public void setWait_time(String wait_time) {
        this.wait_time = wait_time;
    }
}
