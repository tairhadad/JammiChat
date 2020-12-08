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
}
