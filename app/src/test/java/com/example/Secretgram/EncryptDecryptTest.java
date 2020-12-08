package com.example.Secretgram;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class EncryptDecryptTest {

    @Test
    public void SimpleEncryptDecryptTest() throws UnsupportedEncodingException {
        DES des = new DES();
        String key = "testtest";

        String msg = "Encrypt me!";

        System.out.println("\n~~~~~~~~~~~~~~~~~~~");
        String encryptedMsg = des.Cipher(msg, key, 1);
        System.out.println(encryptedMsg);
        System.out.println("~~~~~~~~~~~~~~~~~~~\n");


        System.out.println("~~~~~~~~~~~~~~~~~~~");
        String decryptedMsg = des.Cipher(encryptedMsg, key, 2);
        System.out.println(decryptedMsg);
        System.out.println("~~~~~~~~~~~~~~~~~~~\n");

    }
}
