/*
 * Copyright 2017 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.webcrawler.jsoup;

import com.bc.util.Util;
import com.bc.webcrawler.CrawlMetaData;
import com.bc.webcrawler.Crawler;
import com.bc.webcrawler.CrawlerContext;
import com.bc.webcrawler.ResumeHandler;
import com.bc.webcrawler.ResumeHandlerInMemoryCache;
import com.bc.webcrawler.UrlParser;
import com.bc.webcrawler.UserAgentProvider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 6:03:12 PM
 */
public class ReadMeJsoupCrawler {

    private static final Logger logger = Logger.getLogger(ReadMeJsoupCrawler.class.getName());

    public static class JsoupLinkExtractor implements Function<Document, Set<String>> {
        @Override
        public Set<String> apply(Document doc) {
            final Elements elements = doc.select("a[href]");
            final Set<String> links = elements.isEmpty() ? Collections.EMPTY_SET : new HashSet(elements.size());
            for(Element element : elements) {
                final String link = element.attr("abs:href");
                if(link != null && !link.isEmpty()) {
                    links.add(link);
                }
            }
            return links;
        }
    }
    
    public static class JsoupUrlParser implements UrlParser<Document> {

        private final int timeout;

        private final int maxBodySize;

        private final Function<String, String> userAgentProvider;

        private final Map<String, String> cookies;

        public JsoupUrlParser(int timeout, int maxBodySize) {
            this(new UserAgentProvider(), timeout, maxBodySize);
        }

        public JsoupUrlParser(Function<String, String> userAgentProvider, int timeout, int maxBodySize) {
            this.userAgentProvider = Objects.requireNonNull(userAgentProvider);
            this.timeout = timeout;
            this.maxBodySize = maxBodySize;
            this.cookies = new HashMap<>();
        }


        @Override
        public Document parse(String link) throws MalformedURLException, IOException {

            final URL url = new URL(null, link, new com.bc.net.util.HttpStreamHandlerForBadStatusLine());

            final String userAgent = Objects.requireNonNull(this.userAgentProvider.apply(link));

            logger.finer(() -> "UserAgent: " + userAgent + ", URL: " + link + "\nCookies: " + cookies);

            final Connection.Response res = HttpConnection
                    .connect(url)
                    .userAgent(userAgent)
                    .followRedirects(true)
                    .cookies(cookies)
                    .timeout(timeout)
                    .maxBodySize(maxBodySize)
                    .execute();

            final Map<String, String> cookiesFromRes = res.cookies();

            logger.finer(() -> "Cookies from response: " + cookiesFromRes);

            this.cookies.putAll(cookiesFromRes);

            final Document doc = res.parse();

            return doc;
        }

        @Override
        public Map<String, String> getCookies() {
            return new LinkedHashMap(cookies);
        }
    }
    
    public static void main(String... args) throws IOException {
        
//        try(final InputStream in = Thread.currentThread().getContextClassLoader()
//                .getResourceAsStream("META-INF/configs/logging.properties")) {
//            LogManager.getLogManager().readConfiguration(in);
//        }
        
        final int connectTimeout = 10_000;
        final int readTimeout = 20_000;
        final int maxBodySize = 100_000_000;
        
        final UrlParser<Document> parser;
        
        parser = new JsoupUrlParser(connectTimeout + readTimeout, maxBodySize);
//        parser = new UrlParserImpl(connectTimeout, readTimeout);
//        parser = new OkHttpUrlParser(connectTimeout, readTimeout);
        
        final String baseUrl = "http://www.buzzwears.com";
        final String startUrl = baseUrl;
        final Pattern linkToScrappPattern = Pattern.compile("\\d{1,}_"); //Pattern.compile(".*");
        final Predicate<String> linkToScrappTest = (link) -> linkToScrappPattern.matcher(link).find();
        
        final ResumeHandler resumeHandler = new ResumeHandlerInMemoryCache(
                Collections.EMPTY_SET
        );
        
        final String robotsCss = "meta[name=robots]";
        final Predicate<Document> docIsNoIndex = (doc) -> {
            final Element robots = doc.select(robotsCss).first();
            final String content = robots == null ? null : robots.attr("content");
            return content == null ? false : content.toLowerCase().contains("noindex");
        }; 
        final Predicate<Document> docIsNoFollow = (doc) -> {
            final Element robots = doc.select(robotsCss).first();
            final String content = robots == null ? null : robots.attr("content");
            return content == null ? false : content.toLowerCase().contains("nofollow");
        }; 
        
        final int n = 1;
        
        final CrawlerContext<Document> context = CrawlerContext.builder(Document.class)
                .batchInterval(3_000)
                .batchSize(2)
                .crawlLimit(100 * n)
                .crawlUrlTest((link) -> true)
                .linksExtractor(new JsoupLinkExtractor())
                .maxFailsAllowed(9 * n)
                .pageIsNoIndexTest(docIsNoIndex)
                .pageIsNoFollowTest(docIsNoFollow)
                .parseLimit(10 * n)
                .parseUrlTest((link) -> true)
                .preferredLinkTest(linkToScrappTest)
                .resumeHandler(resumeHandler)
//                .retryOnExceptionTestSupplier(() -> new RetryConnectionFilter(2, 2_000)) 
                .retryOnExceptionTestSupplier(() -> (exception) -> false) 
                .timeoutMillis(3600_000 * n)
                .urlFormatter((link) -> link)
                .urlParser(parser)
                .build();
                
        final Crawler<Document> crawler = context.newCrawler(Collections.singleton(startUrl));

        final long tb4 = System.currentTimeMillis();
        final long mb4 = Util.availableMemory();
        System.out.println(LocalDateTime.now() + ". Memory: " + mb4);
        
        try{
            
            while(crawler.hasNext()) {

                final Document doc = crawler.next();

                final CrawlMetaData metaData = crawler.getMetaData();

    //            System.out.println(LocalDateTime.now());

                System.out.println(MessageFormat.format(
                        "Attempted: {0}, failed: {1}", 
                        metaData.getAttempted(), metaData.getFailed()));

                if(doc == null) {
                    System.err.println("Failed: " + crawler.getCurrentUrl());
                    continue;
                }

                final String url = doc.location();

    //            System.out.println("URL: " + url +  "\nTitle: " + doc.title());

                final boolean isToScrapp = linkToScrappTest.test(url);

                System.out.println("Scrapp: " + isToScrapp + ", URL: " + url);

                if(isToScrapp) {
                    final Element idElem = doc.getElementsByAttributeValue("itemprop", "productID").first();
                    final Element priceElem = doc.getElementsByAttributeValue("itemprop", "price").first();
                    System.out.println("Product. ID: " + (idElem==null?null:idElem.text()) + 
                            ", price: " + (priceElem==null?null:priceElem.text()));
                }
            }
        }finally{
            crawler.shutdown();
        }
        
        
        System.out.println(LocalDateTime.now() + 
                ". Consumed. time: " + (System.currentTimeMillis() - tb4) +
                ", Memory: " + Util.usedMemory(mb4));
    }
}