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

        byte[] data = text.getBytes("UTF-8");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
             String encodedStr = Base64.getEncoder().encodeToString(text.getBytes());
        //    return encodedStr;
             } else {
                return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
             }
            
        } catch (Exception e) {
            return null;
        }
    }//base64encode

    public static String base64decode(String input) {
        try {
            


            // byte[] actualByte = Base64.getDecoder().decode(text);
            // String decodedStr = new String(actualByte);
            // return decodedStr;

           
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                byte[] actualByte = Base64.getDecoder().decode(text);

                String decodedStr = new String(actualByte);
               
                return decodedStr;
            } else {
                byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(data);
                return new String(decodedBytes, "UTF-8");
            }
         
        } catch (Exception e) {
            return null;
        }
    }//base64decode


    private static int getIndex(char c) {
        if (c >= 'A' && c <= 'Z') {
            return c - 'A';
        }
        if (c >= 'a' && c <= 'z') {
            return c - 'a' + 26;
        }
        if (c >= '0' && c <= '9') {
            return c - '0' + 52;
        }
        if (c == '+') {
            return 62;
        }
        if (c == '/') {
            return 63;
        }
        if (c == '=') {
            return 64;
        }
        throw new IllegalArgumentException("Invalid Base64 character: " + c);
    }
}
