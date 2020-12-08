package com.example.Secretgram;

import org.junit.Test;

public class AddAndRemoveAlefTest {

    @Test
    public void addAlefTest(){
        DES des = new DES();

        String msg = "something very nice";
        System.out.println(des.addAlef(msg));

        msg = "Hi";
        System.out.println(des.addAlef(msg));

        msg = "yo";
        System.out.println(des.addAlef(msg));

        msg = "מה המצב";
        System.out.println(des.addAlef(msg));

        msg = "היי";
        System.out.println(des.addAlef(msg));
    }
    @Test
    public void removeAlefTest(){
        DES des = new DES();

        String msg = "אhey";
        System.out.println(des.removeAlef(msg));

        msg = "אמה אהמצאב אאחי";
        System.out.println(des.removeAlef(msg));

        msg = "אyo";
        System.out.println(des.removeAlef(msg));

    }

}
