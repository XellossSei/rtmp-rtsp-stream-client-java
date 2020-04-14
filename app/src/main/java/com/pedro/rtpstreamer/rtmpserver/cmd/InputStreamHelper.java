package com.pedro.rtpstreamer.rtmpserver.cmd;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 流读取简易工具
 */
public class InputStreamHelper {
    public InputStreamHelper() {
    }

    BufferedInputStream bufferedInputStream;

    public void printInputStream(String tag, InputStream inputStream) throws IOException {
        bufferedInputStream = new BufferedInputStream(inputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInputStream));
        String bufLine;
        while ((bufLine = reader.readLine()) != null) {
            Log.d(tag, bufLine);
        }
    }

    public void closeStream() {
        try {
            bufferedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
