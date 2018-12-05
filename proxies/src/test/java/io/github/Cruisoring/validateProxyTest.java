package io.github.Cruisoring;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.workers.Worker;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class validateProxyTest {

    @Test
    public void testHttpProxy(){
        try(Worker worker = Worker.create(DriverType.Chrome)){
            worker.gotoUrl("https://httpbin.org/ip");
            Logger.D("");
        }catch (Exception ex){
            Logger.E(ex);
        }
    }

    @Test
    public void testDump(){
        dump("https://httpbin.org/ip");
    }

    public static void dump(String URLName){
        try {
            DataInputStream di = null;
            FileOutputStream fo = null;
            byte [] b = new byte[1];

            // PROXY
            System.setProperty("https.proxySet", "true");
            System.setProperty("http.proxyHost","37.52.175.105") ;
            System.setProperty("http.proxyPort", "41649") ;

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new
                            PasswordAuthentication("mydomain\\username","password".toCharArray());
                }});

            URL u = new URL(URLName);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            di = new DataInputStream(con.getInputStream());
            while(-1 != di.read(b,0,1)) {
                System.out.print(new String(b));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
