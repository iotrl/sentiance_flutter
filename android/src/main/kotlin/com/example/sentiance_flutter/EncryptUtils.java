package com.example.sentiance_flutter;

import java.util.*;
import android.util.Log;

public class EncryptUtils {
    // public static final String DEFAULT_ENCODING = "UTF-8"; 
    // static BASE64Encoder enc = new BASE64Encoder();
    // static BASE64Decoder dec = new BASE64Decoder();

    public static String base64encode(String text) {
        try {
            // String encodedStr = Base64.getEncoder().encodeToString(text.getBytes());
        //    return encodedStr;

        byte[] data = text.getBytes("UTF-8");
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    } else {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
    }
            
        } catch (Exception e) {
            return null;
        }
    }//base64encode

    public static String base64decode(String text) {
        try {
            // byte[] actualByte = Base64.getDecoder().decode(text);
            // String decodedStr = new String(actualByte);
            // return decodedStr;

            byte[] data = text.getBytes("UTF-8");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
                return new String(decodedBytes, "UTF-8");
            } else {
                byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(data);
                return new String(decodedBytes, "UTF-8");
            }
         
        } catch (Exception e) {
            return null;
        }
    }//base64decode
}
