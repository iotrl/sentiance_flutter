package com.example.sentiance_flutter;

import java.util.*;

import android.os.Build;
import android.util.Log;
import android.util.Base64;
import org.apache.commons.codec.binary.Base64;

public class EncryptUtils {
    // public static final String DEFAULT_ENCODING = "UTF-8"; 
    // static BASE64Encoder enc = new BASE64Encoder();
    // static BASE64Decoder dec = new BASE64Decoder();

   

    public static String base64encode(String text) {
        try {

         
            // String encodedStr = Base64.getEncoder().encodeToString(text.getBytes());
        //    return encodedStr;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
             String encodedStr = android.util.Base64.getEncoder().encodeToString(text.getBytes());
        //    return encodedStr;
             } else {
            byte[] data = text.getBytes("UTF-8");
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

           
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                byte[] actualByte = android.util.Base64.getDecoder().decode(text);

                String decodedStr = new String(actualByte);
               
                return decodedStr;
            } else {
                byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(text);
                return new String(decodedBytes, "UTF-8");
            }
         
        } catch (Exception e) {
            return null;
        }
    }//base64decode


}
