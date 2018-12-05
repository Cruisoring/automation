package io.github.Cruisoring.helpers;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.InetAddress;

public class ProxyMat{

    File file=null;
    static RandomAccessFile read=null;
    public ProxyMat(){
        file=new File("c:/working/proxies.txt");
        try {
            read=new RandomAccessFile(file,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void checkproxies(){
        try{
            String line;
            for(int i=0;i<25;i++){
                if((line=read.readLine())!=null){
                    System.out.println(line);
                    line = line.substring(line.indexOf('/')+1);
                    String[] hp=line.split(":");
                    InetAddress addr=InetAddress.getByName(hp[0]);
                    if(addr.isReachable(5000)){
                        System.out.println("reached");
                        ensocketize(hp[0],Integer.parseInt(hp[1]));
                    }
                }
            }
        }catch(Exception ex){ex.printStackTrace();}
    }



    public void ensocketize(String host,int port){
        try{
            File pros=new File("working.txt");
            HttpClient client=new DefaultHttpClient();
            HttpGet get=new HttpGet("http://blanksite.com/");
            HttpHost proxy=new HttpHost(host,port);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);
            HttpResponse response=client.execute(get);
            HttpEntity enti=response.getEntity();
            if(response!=null){
                System.out.println(response.getStatusLine());
                System.out.println(response.toString());
                System.out.println(host+":"+port+" @@ working");
            }
        }catch(Exception ex){System.out.println("Proxy failed");}
    }

    public static void main(String[] args){
        ProxyMat mat=new ProxyMat();
        mat.checkproxies();
    }
}
