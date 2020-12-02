package com.example.Secretgram;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class StringToBinaryTest {

    @Test
    public void SimpleTextHebrewEnglish() throws UnsupportedEncodingException {
        String hebText = "אא";
        String engText = "אa";
        System.out.println("hebText = " + hebText);
        System.out.println("engText = " + engText);

        //to make it into binary
        String hebBinary = new BigInteger(hebText.getBytes(StandardCharsets.UTF_16BE)).toString(2);
        String engBinary = new BigInteger(engText.getBytes(StandardCharsets.UTF_16BE)).toString(2);
        engBinary = "00000" + engBinary;

        engBinary = engBinary.substring(16);
        engBinary = "0000010111101001" + engBinary;

        System.out.println("hebBinary = " + hebBinary);
        System.out.println("engBinary = " + engBinary);



        //to make it into string
        String reverseHebText = new String(new BigInteger(hebBinary.toString(), 2).toByteArray(), StandardCharsets.UTF_16BE);
        String reverseEngText = new String(new BigInteger(engBinary.toString(), 2).toByteArray(), StandardCharsets.UTF_16BE);

        System.out.println("hebText = " + reverseHebText);
        System.out.println("engText = " + reverseEngText);

    }

    @Test
    public void AdvancedTextHebrewEnglish() throws UnsupportedEncodingException {
        String hebText = "שלום";
        String engText = "hello";
        System.out.println("hebText = " + hebText);
        System.out.println("engText = " + engText);

        //turn them into packets
        ArrayList<String> hebPacket = make_packages(hebText, 1);
        ArrayList<String> engPacket = make_packages(engText, 1);

        //to make it into binary
        ArrayList<String> hebBinary = new ArrayList<>();
        ArrayList<String> engBinary = new ArrayList<>();

        for (String aPackage : hebPacket)
            hebBinary.add(new BigInteger(aPackage.getBytes(StandardCharsets.UTF_16BE)).toString(2));

        for (String aPackage : engPacket)
            engBinary.add(new BigInteger(aPackage.getBytes(StandardCharsets.UTF_16BE)).toString(2));


        //to make it into string

        StringBuilder reverseHebText = new StringBuilder();
        StringBuilder reverseEngText = new StringBuilder();

        System.out.print("hebBinary ?= ");
        for (String a : hebBinary){
            System.out.println(a + ", ");
            reverseHebText.append(new String(new BigInteger(a.toString(), 2).toByteArray(), StandardCharsets.UTF_16BE));
        }

        System.out.print("engBinary ?= ");
        for (String a : engBinary){
            System.out.print(a + ", ");
            reverseEngText.append(new String(new BigInteger(a.toString(), 2).toByteArray(), StandardCharsets.UTF_16BE));
        }
        System.out.println("\ndone!");

        System.out.println("hebText = " + reverseHebText);
        System.out.println("engText = " + reverseEngText.substring(1));

    }



















    public ArrayList<String> make_packages(String msg, int type) throws UnsupportedEncodingException {
        System.out.println("binary msg 0 = " + msg);
        ArrayList<String> packages = new ArrayList<>();
        String binary_msg = "";
        //convert the original message to binary code.
        if (type == 1){
            binary_msg = new BigInteger(msg.getBytes(StandardCharsets.UTF_16BE)).toString(2);
            //binary_msg = "0" + binary_msg;
        }
        else
            binary_msg += msg;

        //cutting the message into 64 bits each
        while (binary_msg.length() > 64)
        {
            packages.add(binary_msg.substring(0,64));
            binary_msg = binary_msg.substring(64);
        }
        //for the last package (if it exist), filling it with zeros at the end.
        // in order to make it also 64 bit.
        if (binary_msg.length() > 0)
        {
            String adder = new String(new char[64 - binary_msg.length()]).replace('\0', '0');
            binary_msg = adder.concat(binary_msg);
            packages.add(binary_msg);
        }
        return packages;
    }
}
