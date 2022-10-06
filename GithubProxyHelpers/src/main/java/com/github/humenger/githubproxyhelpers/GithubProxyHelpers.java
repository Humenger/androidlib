package com.github.humenger.githubproxyhelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import com.qiniu.android.netdiag.Ping;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GithubProxyHelpers {

    private static final int timeout=10*1000;//10s
    private static final List<ProxyRule> proxyRules=new ArrayList<>();
    private static SharedPreferences preferences;
    static {
        proxyRules.add(new ProxyRule(){{id=1000;
            match ="https://raw.githubusercontent.com/";replace="https://raw.fastgit.org/";mirrorHost="raw.fastgit.org";originalHost="raw.githubusercontent.com";}});
        proxyRules.add(new ProxyRule(){{id=1001;
            match ="https://raw.githubusercontent.com/";replace="https://raw.fastgit.org/";mirrorHost="raw.fastgit.org";originalHost="raw.githubusercontent.com";}});
        proxyRules.add(new ProxyRule(){{id=1002;
            match ="https://raw.githubusercontent.com/";replace="https://gh.wget.cool/https://raw.githubusercontent.com/";mirrorHost="gh.wget.cool";originalHost="raw.githubusercontent.com";}});
        proxyRules.add(new ProxyRule(){{id=1003;
            match ="https://raw.githubusercontent.com/";replace="https://y8b4odqg.fast-github.ml/-----https://raw.githubusercontent.com/";mirrorHost="y8b4odqg.fast-github.ml";originalHost="raw.githubusercontent.com";}});
        proxyRules.add(new ProxyRule(){{id=1004;
            match ="https://raw.githubusercontent.com/";replace="https://ghproxy.com/https://raw.githubusercontent.com/";mirrorHost="ghproxy.com";originalHost="raw.githubusercontent.com";}});
        proxyRules.add(new ProxyRule(){{id=1005;
            match ="https://raw.githubusercontent.com/";replace="https://raw.staticdn.net/";mirrorHost="raw.staticdn.net";originalHost="raw.githubusercontent.com";}});
        proxyRules.add(new ProxyRule(){{id=1005;
            match ="https://raw.githubusercontent.com/";replace="https://raw.githubusercontents.com/";mirrorHost="raw.githubusercontents.com";originalHost="raw.githubusercontent.com";}});

        proxyRules.add(new ProxyRule(){{id=2000;
            match ="https://github.com/";replace="https://download.fastgit.org/";mirrorHost="download.fastgit.org";originalHost="github.com";}});
        proxyRules.add(new ProxyRule(){{id=2001;
            match ="https://github.com/";replace="https://gh.wget.cool/https://github.com/";mirrorHost="gh.wget.cool";originalHost="github.com";}});
        proxyRules.add(new ProxyRule(){{id=2002;
            match ="https://github.com/";replace="https://y8b4odqg.fast-github.ml/-----https://github.com/";mirrorHost="y8b4odqg.fast-github.ml";originalHost="github.com";}});
        proxyRules.add(new ProxyRule(){{id=2003;
            match ="https://github.com/";replace="https://ghproxy.com/https://github.com/";mirrorHost="ghproxy.com";originalHost="github.com";}});


//        proxyRules.add(new ProxyRule(){{id=3000;
//            match ="https://api.github.com/";replace="https://raw.staticdn.net/";mirrorHost="raw.staticdn.net";originalHost="raw.githubusercontent.com";}});
    }
    private static void checkDelay() {
        for (ProxyRule proxyRule : proxyRules) {
           checkDelayImpl(proxyRule);
        }
    }

    private static void checkDelayImpl(ProxyRule proxyRule) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                PingV2 pingV2=new PingV2(InetAddress.getByName(proxyRule.mirrorHost), new PingV2.PingListener() {
                    int allTimeMs=0;
                    int failedCount=0;
                    @Override
                    public void onPing(long timeMs, int index) {
                        allTimeMs+=timeMs;
                        proxyRule.delayMs=allTimeMs/(index+1-failedCount);
                        if(index==9){
                            JSONArray jsonArray=new JSONArray();
                            try {
                                jsonArray.put(0,proxyRule.pingFailedProportion);
                                jsonArray.put(1,proxyRule.delayMs);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            preferences.edit().putString(String.valueOf(proxyRule.id),jsonArray.toString()).apply();
                        }
                    }

                    @Override
                    public void onPingException(Exception e, int index) {
                        failedCount++;
                        proxyRule.pingFailedProportion=failedCount/10f*100;
                        if(index==9){
                            JSONArray jsonArray=new JSONArray();
                            try {
                                jsonArray.put(0,proxyRule.pingFailedProportion);
                                jsonArray.put(1,proxyRule.delayMs);
                            } catch (JSONException e2) {
                                e2.printStackTrace();
                            }
                            preferences.edit().putString(String.valueOf(proxyRule.id),jsonArray.toString()).apply();
                        }

                    }
                });
                pingV2.setCount(10);
                pingV2.setTimeoutMs(3000);
                AsyncTask.THREAD_POOL_EXECUTOR.execute(pingV2);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }else {
            Ping.start(proxyRule.mirrorHost, 10, null, r -> {
                proxyRule.delayMs= (int) r.avg;
                proxyRule.pingFailedProportion=r.dropped/10f*100;
            });

        }
    }
    public static void init(Context context){
        preferences=context.getSharedPreferences("github_proxy_helpers",Context.MODE_PRIVATE);
        initData();
        new Thread(GithubProxyHelpers::checkDelay).start();
    }

    private static void initData() {
        for (ProxyRule proxyRule : proxyRules) {
            try {
                JSONArray jsonArray=new JSONArray(preferences.getString(String.valueOf(proxyRule.id),""));
                if(jsonArray.length()>=2){
                    proxyRule.pingFailedProportion= (float) jsonArray.optDouble(0);
                    proxyRule.delayMs=jsonArray.optInt(1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public static void addProxyRule(ProxyRule proxyRule){
        if(proxyRule==null)return;
        if(proxyRule.id<10000){
            throw new IllegalArgumentException("proxyRule.id should not be less than 10000");
        }
        for (ProxyRule rule : proxyRules) {
            if(rule.id==proxyRule.id){
                throw new IllegalArgumentException("proxyRule.id repeat");
            }
        }
        try {
            JSONArray jsonArray=new JSONArray(preferences.getString(String.valueOf(proxyRule.id),""));
            if(jsonArray.length()>=2){
                proxyRule.pingFailedProportion= (float) jsonArray.optDouble(0);
                proxyRule.delayMs=jsonArray.optInt(1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        checkDelayImpl(proxyRule);
        proxyRules.add(proxyRule);
    }

    public static String getProxyUrl(String originalUrl){
        Collections.sort(proxyRules);
        for (ProxyRule proxyRule : proxyRules) {
            if(proxyRule.isFailed())continue;
            return originalUrl.replace(proxyRule.match,proxyRule.replace);
        }
        return originalUrl;
    }

}