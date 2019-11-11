package com.bc.webcrawler;

import com.bc.net.RetryConnectionFilter;
import com.bc.net.util.UserAgents;
import com.bc.util.Util;
import com.bc.webcrawler.predicates.HtmlLinkIsToBeCrawledTest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.junit.Test;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 23, 2018 8:25:09 AM
 */
public class ReadMeDOMCrawlerTest extends TestBase {

    public final static class Node {
        public final int pos;
        public final HTML.Tag startTag;
        public final MutableAttributeSet attributes;
        public final char [] text;
        public final HTML.Tag endTag;
        public Node(int pos, HTML.Tag startTag, MutableAttributeSet attributes, char[] text, HTML.Tag endTag) {
            this.pos = pos;
            this.startTag = startTag;
            this.attributes = attributes == null ? null : new SimpleAttributeSet(attributes);
            this.text = text == null ? null : Arrays.copyOf(text, text.length);
            this.endTag = endTag;
        }
        @Override
        public String toString() {
            final StringBuilder b = new StringBuilder();
            if(startTag == null && endTag == null) {
                if(text != null) b.append(text);
            }else{
                b.append('<').append(startTag);
                if(attributes != null) b.append(' ').append(attributes);
                if(endTag == null) b.append('/');
                b.append('>');
                if(text != null) b.append(text);
                if(endTag != null) b.append('<').append('/').append(endTag).append('>');
            }
            return b.toString();
        }
    }
    
    public static class LinkExtractorImpl implements Function<List, Set<String>> {
        @Override
        public Set<String> apply(List nodes) {
            final Set<String> output = new LinkedHashSet<>();
            for(Object obj : nodes) {
                final Node node = (Node)obj;
//                System.out.println(node);
                if(node.startTag == HTML.Tag.A) {
//                    System.out.println("Found<A>: " + node);
                    final Object href = node.attributes.getAttribute(HTML.Attribute.HREF);
//                    System.out.println("Found<A HREF>: " + href);
                    if(href != null) {
                        output.add(href.toString());
                    }
                }
            }
            return output;
        }
    }
    
    public static class ParserCallbackImpl extends HTMLEditorKit.ParserCallback {
        private final List<Node> startTags = new ArrayList<>();
        private final List<Node> nodeList = new ArrayList<>();
        @Override
        public void handleEndOfLineString(String eol) { }
        @Override
        public void handleError(String errorMsg, int pos) { }
        @Override
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            nodeList.add(new Node(pos, t, a, null, null));
        }
        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            startTags.add(new Node(pos, t, a, null, null));
        }
        @Override
        public void handleEndTag(HTML.Tag t, int pos) {
            final Node start = startTags.remove(startTags.size() - 1);
            nodeList.add(new Node(start.pos, start.startTag, start.attributes, null, t));
        }
        @Override
        public void handleComment(char[] data, int pos) { }
        @Override
        public void handleText(char[] data, int pos) {
            nodeList.add(new Node(pos, null, null, data, null));
        }
        @Override
        public void flush() throws BadLocationException { }
        public void reset() {
            this.startTags.clear();
            this.nodeList.clear();
        }
        public List<Node> getNodeList() {
            return new ArrayList(nodeList);
        }
    }
    
    public static class UrlParserImpl implements UrlParser<List<Node>> {
        private final UserAgents userAgents = new UserAgents();
        private final ParserDelegator parserDelegator;
        private final ParserCallbackImpl parserCallback;
        public UrlParserImpl() {
            this.parserDelegator = new ParserDelegator();
            this.parserCallback = new ParserCallbackImpl();
        }
        @Override
        public List<Node> parse(String link) throws IOException {
            
            this.parserCallback.reset();
            
            final URL url = new URL(null, link, new com.bc.net.util.HttpStreamHandlerForBadStatusLine());
//            final URL url = new URL(link);
            final URLConnection conn = url.openConnection();
            final Charset charset = StandardCharsets.UTF_8;
            conn.setRequestProperty("Accept-Charset", charset.name());
            conn.setRequestProperty("User-Agent", userAgents.any(link));
            if(conn instanceof HttpURLConnection) {
                final HttpURLConnection httpConn = (HttpURLConnection)conn;
                System.out.println("Response. Code: " + httpConn.getResponseCode() + 
                        ", message: " + httpConn.getResponseMessage() + ", link: " + link);
            }else{
                System.out.println("Link: " + link);
            }
            
            try(Reader reader = new InputStreamReader(conn.getInputStream(), charset)) {
                this.parserDelegator.parse(reader, this.parserCallback, true);
            }
//            try(InputStream in = url.openStream();
//                    Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
//                this.parserDelegator.parse(reader, this.parserCallback, true);
//            }
            return this.parserCallback.getNodeList();
        }

        @Override
        public List<String> getCookieNameValueList() {
            return Collections.EMPTY_LIST;
        }
        @Override
        public Map<String, String> getCookieNameValueMap() {
            return Collections.EMPTY_MAP;
        }
    }

    public ReadMeDOMCrawlerTest() { }
    
    @Test
    public void test() {
        
//        new ReadMeDOMCrawlerTest().crawl("http://www.buzzwears.com");

        final Predicate<String> guardianPreferredLink = (link) -> link.contains("/news/");
        new ReadMeDOMCrawlerTest().crawl("https://guardian.ng/", guardianPreferredLink);
    }

    public void crawl(String startUrl) {
        this.crawl(startUrl, (link) -> false);
    }
    
    public void crawl(String startUrl, Predicate<String> preferredLinkTest) {
        
        final ContentTypeRequest contentTypeReq = new ContentTypeRequestOkHttp();
    
        final Predicate<String> crawlHtmlLinks = new HtmlLinkIsToBeCrawledTest(
                contentTypeReq, 7_000, 7_000, true);
        
        final UrlParser<List<Node>> urlParser = new UrlParserImpl();
        
        final CrawlerContext<List> crawlerContext = CrawlerContext.builder(List.class)
                .asyncLinkCollection(true)
                .baseUrl(startUrl)
                .batchInterval(5_000)
                .batchSize(5)
                .crawlLimit(100)
                .crawlUrlTest(crawlHtmlLinks)
                .linksExtractor(new LinkExtractorImpl())
                .maxFailsAllowed(100)
                .pageIsNoIndexTest((page) -> false)
                .pageIsNoFollowTest((page) -> false)
                .parseLimit(10)
                .parseUrlTest((url) -> true)
                .preferredLinkTest(preferredLinkTest)
                .resumeHandler(new ResumeHandlerInMemoryStore(Collections.EMPTY_SET))
                .retryOnExceptionTestSupplier(() -> new RetryConnectionFilter(2, 2_000L))
                .timeoutMillis(20_000)
                .urlFormatter((url) -> url)
                .urlParser(urlParser)
                .build();

      final Crawler<List> crawler = crawlerContext.newCrawler(Collections.singleton(startUrl));    

        final long mb4 = Util.availableMemory();
        final long tb4 = System.currentTimeMillis();
        System.out.println("Beginning crawl. Memory: " + mb4);
        
        try{
            
            crawler.stream().forEach((nodeList) -> {
                System.out.println("\nPrinting Nodes");
                if(nodeList == null) {
                    System.out.println("Didn ot extract anything");
                    return;
                }
                String title = null;
                for(Object obj : nodeList) {
                    
//                    System.out.println(obj);
                    
                    final Node node = (Node)obj;
                    if(node.text != null) {
                        title = new String(node.text);
                    }
                    if(node.startTag == HTML.Tag.TITLE) {
                        if(node.text != null) {
                            title = new String(node.text);
                        }
                        break;
                    }
                }
                System.out.println("Extracted page containing " + nodeList.size() + " nodes, with title: " + title);
            });
        }finally{
            
            System.out.println("Concluded crawl. Consumed. memory: " +
                    (Util.usedMemory(mb4)) + ", time: " + 
                    (System.currentTimeMillis() - tb4) + " millis");
            
            crawler.shutdown(3, TimeUnit.SECONDS);
        }
    }
}
/**
 * 
Async 
Concluded crawl. Consumed. memory: 7_469_320, time: 29_545 millis
Aug 11, 2018 4:04:26 AM com.bc.webcrawler.CrawlerImpl shutdown
FINE: SHUTTING DOWN com.bc.webcrawler.CrawlerImpl@5f150435{
Time: 29545/120000. URLs:: attempted: 10, failed: 0, remaining: 7
com.bc.webcrawler.CrawlerContext@1c53fd30{crawlQueryUrls=true, parseLimit=10, crawlLimit=100, batchSize=5, batchInterval=5000, timeoutMillis=120000, connectTimeoutForLinkValidation=5000, readTimeoutForLinkValidation=10000}
}

Sync
Concluded crawl. Consumed. memory: 5_313_536, time: 163_142 millis
Aug 11, 2018 4:04:26 AM com.bc.webcrawler.CrawlerImpl shutdown
FINE: SHUTTING DOWN com.bc.webcrawler.CrawlerImpl@5f150435{
Time: 163142/120000. URLs:: attempted: 1, failed: 0, remaining: 100
com.bc.webcrawler.CrawlerContext@1c53fd30{crawlQueryUrls=true, parseLimit=10, crawlLimit=100, batchSize=5, batchInterval=5000, timeoutMillis=120000, connectTimeoutForLinkValidation=5000, readTimeoutForLinkValidation=10000}
}
 * 
 */
