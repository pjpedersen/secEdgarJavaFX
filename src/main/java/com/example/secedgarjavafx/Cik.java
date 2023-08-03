package com.example.secedgarjavafx;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Cik {

        HashMap<String, String> cikTickers = new HashMap<String, String>();


    //creating File instance to reference text file in Java
    File text = new File("C:/temp/test.txt");

    //Creating Scanner instance to read File in Java
    Scanner scnr = new Scanner(text);


    public Cik() throws FileNotFoundException {
    }
}
