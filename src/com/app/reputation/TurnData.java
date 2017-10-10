package com.app.reputation;

import java.nio.charset.Charset;

import org.json.JSONObject;

public class TurnData {
	
    public String data = "";
	
	static public TurnData unpersist(byte[] byteArray) {
		return null;
	
	}
	
    // This is the byte array we will write out to the TBMP API.
    public byte[] persist() {
        JSONObject retVal = new JSONObject();

        try {
            retVal.put("data", data);
//            retVal.put("turnCounter", turnCounter);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String st = retVal.toString();

//        Log.d(TAG, "==== PERSISTING\n" + st);

        return st.getBytes(Charset.forName("UTF-8"));
    }

}
