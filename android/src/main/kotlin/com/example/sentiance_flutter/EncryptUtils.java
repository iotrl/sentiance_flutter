package com.example.sentiance_flutter;

import java.util.*;
import android.util.Log;

public class EncryptUtils {
    // public static final String DEFAULT_ENCODING = "UTF-8"; 
    // static BASE64Encoder enc = new BASE64Encoder();
    // static BASE64Decoder dec = new BASE64Decoder();

    public static String base64encode(String text) {
        try {
            String encodedStr = Base64.getEncoder().encodeToString(text.getBytes());
          //  Log.e("SEncription ", text +" "+ encodedStr  );
            return encodedStr;
        } catch (Exception e) {
            return null;
        }
    }//base64encode

    public static String base64decode(String text) {
        try {
            byte[] actualByte = Base64.getDecoder().decode(text);

            String decodedStr = new String(actualByte);
           // Log.e("SEncription ", text +" "+ decodedStr  );
            return decodedStr;
        } catch (Exception e) {
            return null;
        }
    }//base64decode
}