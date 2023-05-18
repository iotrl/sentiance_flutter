package com.example.sentiance_flutter;

import java.util.*;

import android.os.Build;
import android.util.Log;
import android.util.Base64;

public class EncryptUtils {
    // public static final String DEFAULT_ENCODING = "UTF-8"; 
    // static BASE64Encoder enc = new BASE64Encoder();
    // static BASE64Decoder dec = new BASE64Decoder();

    private static final char[] BASE64_ALPHABET =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();


    public static String base64encode(String input) {
        try {

            byte[] data = input.getBytes();
        StringBuilder encoded = new StringBuilder();

        int paddingCount = (3 - (data.length % 3)) % 3;

        for (int i = 0; i < data.length; i += 3) {
            int b1 = data[i] & 0xFF;
            int b2 = data[i + 1] & 0xFF;
            int b3 = data[i + 2] & 0xFF;

            int index1 = b1 >> 2;
            int index2 = ((b1 & 0x03) << 4) | (b2 >> 4);
            int index3 = ((b2 & 0x0F) << 2) | (b3 >> 6);
            int index4 = b3 & 0x3F;

            encoded.append(BASE64_ALPHABET[index1]);
            encoded.append(BASE64_ALPHABET[index2]);
            encoded.append(BASE64_ALPHABET[index3]);
            encoded.append(BASE64_ALPHABET[index4]);
        }

        for (int i = 0; i < paddingCount; i++) {
            encoded.setCharAt(encoded.length() - 1 - i, '=');
        }

        return encoded.toString();
            // String encodedStr = Base64.getEncoder().encodeToString(text.getBytes());
        //    return encodedStr;

        // byte[] data = text.getBytes("UTF-8");
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        //      String encodedStr = Base64.getEncoder().encodeToString(text.getBytes());
        // //    return encodedStr;
        //      } else {
        //         return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
        //      }
            
        } catch (Exception e) {
            return null;
        }
    }//base64encode

    public static String base64decode(String input) {
        try {
            StringBuilder decoded = new StringBuilder();

            int paddingCount = 0;
            for (int i = input.length() - 1; i >= 0; i--) {
                if (input.charAt(i) == '=') {
                    paddingCount++;
                } else {
                    break;
                }
            }
    
            int length = input.length();
            int numGroups = length / 4;
    
            for (int group = 0; group < numGroups; group++) {
                int start = group * 4;
                int index1 = getIndex(input.charAt(start));
                int index2 = getIndex(input.charAt(start + 1));
                int index3 = getIndex(input.charAt(start + 2));
                int index4 = getIndex(input.charAt(start + 3));
    
                int b1 = (index1 << 2) | (index2 >> 4);
                int b2 = ((index2 & 0x0F) << 4) | (index3 >> 2);
                int b3 = ((index3 & 0x03) << 6) | index4;
    
                decoded.append((char) b1);
                if (index3 != 64) {
                    decoded.append((char) b2);
                }
                if (index4 != 64) {
                    decoded.append((char) b3);
                }
            }
    
            return decoded.toString();


            // byte[] actualByte = Base64.getDecoder().decode(text);
            // String decodedStr = new String(actualByte);
            // return decodedStr;

            // byte[] data = text.getBytes("UTF-8");
            // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            //     byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
            //     return new String(decodedBytes, "UTF-8");
            // } else {
            //     byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(data);
            //     return new String(decodedBytes, "UTF-8");
            // }
         
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
