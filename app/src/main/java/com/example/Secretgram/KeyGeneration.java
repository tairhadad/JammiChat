package com.example.Secretgram;
import java.math.BigInteger;
import java.util.ArrayList;

class KeyGeneration {

    private ArrayList<String> Keys = new ArrayList<>(); //Array of 16 keys

    // This function convert string to binary
    String ConvertBinary(String key) {

        //convert the original message to binary code.
        String binary_msg = new BigInteger(key.getBytes()).toString(2);
        if (binary_msg.length() > 0)
            if (binary_msg.length() < 64) {
                String adder = new String(new char[64 - binary_msg.length()]).replace('\0', '0');
                binary_msg = adder.concat(binary_msg);
            }
        return binary_msg;
    }

    private String ShiftLeft(String s, int i){
        String temp;
        for (int j = 0; j < i; j++){
            temp = ""+ s.charAt(0);
            s = s.substring(1) + temp;
        }
        return (s);
    }

    //In this function we have 2 stages of PC : PC-1 and PC-2 .
    String PC(String binaryKey, int pcNum) {
        StringBuilder Pc1Key = new StringBuilder();
        StringBuilder Pc2Key = new StringBuilder();

        int[] PC1 = {57,49,41,33,25,17,9,1,58,50,42,34,26,18, 10,2,59,51,43,35,27, 19,11,3,
                60,52,44,36,63,55,47,39,31,23,15,7,62,54,46,38,30,22,14,6,61,53,45,37,29,
                21,13,5,28,20,12,4};

        int[] PC2 = {14,17,11,24,1,5,3,28,15,6,21,10,23,19,12,4,26,8,16,7,27,20,13,2,
                41,52,31,37,47,55,30,40,51,45,33,48,44,49,39,56,34,53,46,42,50,36,29,32};

        if (pcNum==1) {
            char [] keyArray1 = binaryKey.toCharArray();
            for (int i = 0; i < 56; i++)
                Pc1Key.append(keyArray1[PC1[i] - 1]);
            return Pc1Key.toString();
        }

        if(pcNum==2) {
            char [] keyArray2 = binaryKey.toCharArray();
            for (int i = 0; i < 48; i++)
                Pc2Key.append(keyArray2[PC2[i] - 1]);
            return Pc2Key.toString();
        }
        return null;
    }

    //This function responsible for split the message, doing the shift action and create 16 keys.
    ArrayList split_and_round(String binaryKey) {
        String C0 , D0;
        String C1, D1;
        final int size =binaryKey.length()/2;
        C0 = binaryKey.substring(0,size);
        D0= binaryKey.substring(size);
        String FinalKey;
        for (int i=1; i<17; i++) {
            if(i==1 || i==2 || i==9 || i==16) {
                C1 = ShiftLeft(C0,1);
                D1 = ShiftLeft(D0,1);
            }
            else {
                C1 = ShiftLeft(C0,2);
                D1 = ShiftLeft(D0,2);
                //C1 = ShiftLeft(C1,i);
                //D1 = ShiftLeft(D1,i);
            }
            binaryKey=C1+D1;
            //System.out.println(binaryKey);
            FinalKey = PC(binaryKey,2);
            Keys.add(FinalKey);
            C0=C1;
            D0=D1;
        }
        return Keys;
    }
}