package com.example.Secretgram;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class RSA {

    private final static SecureRandom random = new SecureRandom();
    private BigInteger prime;
    private BigInteger Kpri;

    public void setPrime(BigInteger prime_){
        prime = prime_;
    }

    public void setKpri(BigInteger Kpri_){
        Kpri = Kpri_;
    }


    public BigInteger getPrime(){
        return prime;
    }

    public BigInteger getKpri(){
        return Kpri;
    }

    public ArrayList<String> Make_RSA(){
        //initiate prime
        BigInteger prime = BigInteger.probablePrime(64, random);

        //initiate alpha
        String alpha_string = String.valueOf(ThreadLocalRandom.current().nextInt(2, 200));
        BigInteger alpha = new BigInteger(alpha_string);

        ArrayList<String> list = new ArrayList<>();
        list.add(String.valueOf(alpha));
        list.add(String.valueOf(prime));

        return list;
    }

    public ArrayList<String> Build_Keys(String alpha_, String prime_){

        BigInteger alpha = new BigInteger(alpha_);
        BigInteger prime = new BigInteger(prime_);


        int prime_num = prime.intValue();

        if (prime_num < 0)
        {
            prime_num *= -1;
        }

        //A set private and public key

        Kpri = new BigInteger(String.valueOf(ThreadLocalRandom.current().nextInt(2, prime_num -1)));


        BigInteger Kpub;
        Kpub = alpha.modPow(Kpri, prime);

        ArrayList<String> list = new ArrayList<>();
        list.add(String.valueOf(Kpub));
        list.add(String.valueOf(Kpri));

        setPrime(prime);
        setKpri(Kpri);

        return list;

    }

    public String Make_ConnectionKey(String other_public_){

        BigInteger prime = getPrime();
        BigInteger Kpri = getKpri();

        BigInteger other_public = new BigInteger(other_public_);

        BigInteger KAB = other_public.modPow(Kpri, prime);

        StringBuilder key = new StringBuilder(String.valueOf(KAB));

        if (key.length() > 8)
            key = new StringBuilder(key.substring(0, 8));
        else
        {
            while (key.length() <= 8){
                key.append("0");
            }
        }

        return key.toString();

    }

}
