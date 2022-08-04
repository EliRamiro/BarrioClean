package com.aply.barrioclean.utils;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RespuestaResponse {
    int ide_error;
    String msg_error;
    String respuesta;

    public RespuestaResponse() {
    }

    public int getIde_error() {
        return ide_error;
    }

    public void setIde_error(int ide_error) {
        this.ide_error = ide_error;
    }

    public String getMsg_error() {
        return msg_error;
    }

    public void setMsg_error(String msg_error) {
        this.msg_error = msg_error;
    }

    public JSONArray getRespuesta() {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = (JSONArray) (new JSONParser()).parse(this.respuesta);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

}
