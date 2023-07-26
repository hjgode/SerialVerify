package com.hjgode.serialverify;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannelGroup;

public class RawPrinter {
    static String TAG="RawPrinter";
    Inet4Address ipAddress=null;
    int portNumber=9100;

    public RawPrinter(Inet4Address ip4, int port){
        ipAddress=ip4;
        portNumber=port;
    }

    public void doPrint(String s){
        String[] strings = new String[]{s};
        new PrintStringClass().execute(strings);
    }
    class PrintStringClass extends AsyncTask<String, Integer, Integer>{
        // AsyncTask<Params, Progress, Result>
        @Override
        protected Integer doInBackground(String... strings) {
            int count=strings.length;
            for (int i=0; i<count;i++){
                print(strings[i]);
                publishProgress((int) ((i / (float) count) * 100));
                if (isCancelled())
                    break;
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
            Log.d(TAG, "progress " + Integer.toString(progress[0]));
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Printing done " + result + " bytes");
        }

    }
    int print(String printdata) {

        DataOutputStream outToServer=null;
        Socket clientSocket;
        int ret=0;
        try {
            //createHtmlDocument(htmlString);
            //FileInputStream fileInputStream = new FileInputStream(android.os.Environment.getExternalStorageDirectory()  + java.io.File.separator+ "test.pdf");
            InputStream fileInputStream = new ByteArrayInputStream(printdata.getBytes());
            InputStream is =fileInputStream;
            clientSocket = new Socket(ipAddress, portNumber);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            byte[] buffer = new byte[3000];
            while (is.read(buffer) !=-1){
                outToServer.write(buffer);
            }
            outToServer.flush();
            ret = 1;
        } catch (ConnectException connectException){
            Log.e(TAG, connectException.toString());
            ret = -1;
        } catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage());
            ret = -2;
        } catch (IOException  e) {
            Log.e(TAG, e.getMessage());
            ret = -3;
        }

        if (outToServer != null) {
            try {
                outToServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
