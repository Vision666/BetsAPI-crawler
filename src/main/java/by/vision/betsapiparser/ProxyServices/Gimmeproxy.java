package by.vision.betsapiparser.ProxyServices;

import by.vision.betsapiparser.Connection;
import java.util.ArrayList;

public class Gimmeproxy implements ProxyService {
    String ip;
    int port;
    //this request consist of parameters:
    // api key,
    // proxy is support user-agent string,
    // protocol is HTTP,
    // resp will be in format "ip:port"
    private final String REQUEST = "https://gimmeproxy.com/api/getProxy?api_key=41162371-665a-42a7-abf6-23572bc9fe91&referer=true&user-agent=true&protocol=http&ipPort=true";
    private ArrayList<String> proxyList;

    @Override
    public ArrayList<String> toList() {
        return proxyList;
    }

    private String getIp(){
        return ip;
    }

    private int getPort(){
        return port;
    }
}
