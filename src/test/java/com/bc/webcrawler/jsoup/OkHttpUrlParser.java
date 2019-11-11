/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bc.webcrawler.jsoup;

import com.bc.webcrawler.UserAgentProvider;
import com.bc.webcrawler.UrlParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 6, 2018 6:44:04 PM
 */
public class OkHttpUrlParser implements UrlParser<Document>, CookieJar {

    private static final Logger logger = Logger.getLogger(OkHttpUrlParser.class.getName());

    private final Function<String, String> userAgentProvider;

    private final Set<String> cookies;
    
    private final OkHttpClient client;

    public OkHttpUrlParser(int connectTimeout, int readTimeout) {
        this(new UserAgentProvider(), connectTimeout, readTimeout);
    }

    public OkHttpUrlParser(Function<String, String> userAgentProvider, 
            int connectTimeout, int readTimeout) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .cookieJar(this)
                .build();
        this.userAgentProvider = Objects.requireNonNull(userAgentProvider);
        this.cookies = new LinkedHashSet<>();
    }
  

    @Override
    public Document parse(String link) throws MalformedURLException, IOException {

        final URL url = new URL(null, link, new com.bc.net.util.HttpStreamHandlerForBadStatusLine());

        final String userAgent = Objects.requireNonNull(this.userAgentProvider.apply(link));
        
        logger.finer(() -> "UserAgent: " + userAgent + ", URL: " + link + "\nCookies: " + cookies);

        final Request request = new Request.Builder()
            .addHeader("User-Agent", userAgent)
            .url(url)
            .build();
        
        try(final Response response = this.client.newCall(request).execute()) {
        
            final ResponseBody responseBody = response.body();

            final Document doc = Jsoup.parse(responseBody.byteStream(), "UTF-8", link);

            return doc;
        }
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        final List<String> cookiesFromRes = cookies.stream().map((cookie) -> cookie.name() + '=' + cookie.value()).collect(Collectors.toList());
        logger.finer(() -> "Cookies from response: " + cookiesFromRes);
        this.cookies.addAll(cookiesFromRes);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return this.cookies.stream().map((setCookieHeaderValue) -> Cookie.parse(url, setCookieHeaderValue)).collect(Collectors.toList());
    }

    @Override
    public List<String> getCookieNameValueList() {
        return Collections.unmodifiableList(new ArrayList(this.cookies));
    }

    @Override
    public Map<String, String> getCookieNameValueMap() {
        if(cookies.isEmpty()) {
            return Collections.EMPTY_MAP;
        }else{
            final Map<String, String> cookieMap = new HashMap(cookies.size(), 1.0f);
            cookies.stream().forEach((cookie) -> {
                final String [] parts = cookie.split("=");
                cookieMap.put(parts[0], parts[1]);
            });
            return cookieMap;
        }
    }
}
