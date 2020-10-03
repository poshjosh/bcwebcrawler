package com.bc.webcrawler.jsoup;

import com.bc.net.util.UserAgents;
import com.bc.webcrawler.OkHttp;
import com.bc.webcrawler.UrlParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 6, 2018 5:41:00 PM
 */
public class UrlParserImpl implements UrlParser<Document> {

    private transient static final Logger LOG = Logger.getLogger(UrlParserImpl.class.getName());

    private final UserAgents userAgents = new UserAgents();
    
    public UrlParserImpl() { }

    @Override
    public Document parse(String link) throws MalformedURLException, IOException {

        final URL url = new URL(null, link, new com.bc.net.util.HttpStreamHandlerForBadStatusLine());

        LOG.finer(() -> ", URL: " + link);

        final Request request = new Request.Builder()
                .header("User-Agent", userAgents.any(link))
                .url(url).build();

        try(final Response response = OkHttp.getDefaultClient().newCall(request).execute()) {
        
            try(final InputStream in = response.body().byteStream()) {

                final Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), link);

                return doc;
            }
        }
    }
}
