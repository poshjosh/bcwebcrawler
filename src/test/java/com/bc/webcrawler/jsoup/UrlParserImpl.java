package com.bc.webcrawler.jsoup;

import com.bc.net.impl.RequestBuilderImpl;
import com.bc.webcrawler.UrlParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.bc.net.RequestBuilder;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 6, 2018 5:41:00 PM
 */
public class UrlParserImpl implements UrlParser<Document> {

    private static final Logger logger = Logger.getLogger(UrlParserImpl.class.getName());

    private final int connectTimeout;
    
    private final int readTimeout;
    
    private final RequestBuilder reqBuilder;

    public UrlParserImpl(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.reqBuilder = new RequestBuilderImpl();
    }

    @Override
    public Document parse(String link) throws MalformedURLException, IOException {

        final URL url = new URL(null, link, new com.bc.net.util.HttpStreamHandlerForBadStatusLine());

        logger.finer(() -> ", URL: " + link + "\nCookies: " + this.reqBuilder.getCookies());

        try(final InputStream in = this.reqBuilder
                .randomUserAgent(true)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .followRedirects(true).getInputStream(url)) {
        
            final Document doc = Jsoup.parse(in, "UTF-8", link);

            return doc;
        }
    }

    @Override
    public List<String> getCookieList() {
        return this.reqBuilder.getCookies();
    }

    @Override
    public Map<String, String> getCookies() {
        throw new UnsupportedOperationException();
    }
}
