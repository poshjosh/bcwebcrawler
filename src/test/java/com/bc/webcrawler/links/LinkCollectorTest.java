package com.bc.webcrawler.links;

import com.bc.net.RetryConnectionFilter;
import com.bc.util.Util;
import com.bc.webcrawler.ContentTypeRequest;
import com.bc.webcrawler.ContentTypeRequestOkHttp;
import com.bc.webcrawler.Crawler;
import com.bc.webcrawler.CrawlerContext;
import com.bc.webcrawler.ReadMeDOMCrawlerTest;
import com.bc.webcrawler.util.InMemoryStore;
import com.bc.webcrawler.UrlParser;
import com.bc.webcrawler.predicates.CrawlUrlTest;
import com.bc.webcrawler.predicates.HtmlLinkIsToBeCrawledTest;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.text.html.HTML;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author USER
 */
public class LinkCollectorTest {
    
    public LinkCollectorTest() { }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of collectLinks method, of class LinkCollector.
     */
    @Test
    public void testCollectLinks_String_GenericType() {
        System.out.println("collectLinks");
        Object doc = null;
        final LinkCollector instance = getLinkCollector();
        instance.collectLinks(doc, this.getLinkConsumer());
    }

    /**
     * Test of collectLinks method, of class LinkCollector.
     */
    @Test
    public void testCollectLinks_String_Set() {
        System.out.println("collectLinks");
        Set<String> linksToCollect = null;
        final LinkCollector instance = getLinkCollector();
        instance.collectLinks(linksToCollect, this.getLinkConsumer());
    }

    /**
     * Test of getCollected method, of class LinkCollector.
     */
    @Test
    public void testGetCollected() {
        System.out.println("getCollected");
        final LinkCollector instance = getLinkCollector();
        int expResult = 0;
        int result = instance.getCollected();
        assertEquals(expResult, result);
    }

    /**
     * Test of isShutdown method, of class LinkCollector.
     */
    @Test
    public void testIsShutdown() {
        System.out.println("isShutdown");
        final LinkCollector instance = getLinkCollector();
        boolean expResult = false;
        boolean result = instance.isShutdown();
        assertEquals(expResult, result);
    }

    /**
     * Test of isToBeCollected method, of class LinkCollector.
     */
    @Test
    public void testIsToBeCollected() {
        System.out.println("isToBeCollected");
        String link = "";
        final LinkCollector instance = getLinkCollector();
        boolean expResult = false;
        boolean result = instance.isToBeCollected(link);
        assertEquals(expResult, result);
    }

    /**
     * Test of isWithinCollectLimit method, of class LinkCollector.
     */
    @Test
    public void testIsWithinCollectLimit() {
        System.out.println("isWithinCollectLimit");
        final LinkCollector instance = getLinkCollector();
        boolean expResult = false;
        boolean result = instance.isWithinCollectLimit();
        assertEquals(expResult, result);
    }

    /**
     * Test of shutdown method, of class LinkCollector.
     */
    @Test
    public void testShutdown() {
        System.out.println("shutdown");
        long timeout = 0L;
        TimeUnit timeUnit = null;
        final LinkCollector instance = getLinkCollector();
        instance.shutdown(timeout, timeUnit);
    }
    
    public LinkCollector getLinkCollector() {
        
        final String url = "http://www.abc.com";
        
        final UrlParserImpl urlParser = new UrlParserImpl();
        
        return new LinkCollectorAsync(
                this.getCrawlerContext(url, urlParser), 
                url);
    }
    
    public Consumer<String> getLinkConsumer() {
        
        final Consumer<String> printLinks = (link) -> System.out.println(link);
        
        return printLinks;
    }

    public void a(CrawlerContext<List> crawlerContext, String startUrl) {
        
      final Crawler<List> crawler = crawlerContext
                .newCrawler(Collections.singleton(startUrl));    

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
                    
                    final ReadMeDOMCrawlerTest.Node node = (ReadMeDOMCrawlerTest.Node)obj;
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
    
    public CrawlerContext<List> getCrawlerContext(String link, UrlParser urlParser) {
        
        final ContentTypeRequest contentTypeReq = new ContentTypeRequestOkHttp();
    
        final CrawlUrlTest crawlHtmlLinks = new HtmlLinkIsToBeCrawledTest(
                contentTypeReq, 7_000, 7_000, true);
        
        final CrawlerContext<List> crawlerContext = CrawlerContext.builder(List.class)
                .asyncLinkCollection(true)
                .baseUrl(link)
                .batchInterval(5_000)
                .batchSize(5)
                .crawlLimit(100)
                .crawlUrlTest(crawlHtmlLinks)
                .linksExtractor(new ReadMeDOMCrawlerTest.LinkExtractorImpl())
                .maxFailsAllowed(100)
                .pageIsNoIndexTest((page) -> false)
                .pageIsNoFollowTest((page) -> false)
                .parseLimit(10)
                .parseUrlTest((url) -> true)
//                .preferredLinkTest(preferredLinkTest)
                .linkStore(new InMemoryStore())
                .retryOnExceptionTestSupplier(() -> new RetryConnectionFilter(2, 2_000L))
                .timeoutMillis(20_000)
                .urlFormatter((url) -> url)
                .urlParser(urlParser)
                .build();
        
        return crawlerContext;
    }
    
    private static class UrlParserImpl implements UrlParser<String>, Predicate<String>{
        
        private final Set<String> attemptedUrls = Collections.synchronizedSet(new HashSet<>());

        @Override
        public boolean test(String t) {
            synchronized(attemptedUrls) {
                return attemptedUrls.contains(t);
            }
        }

        @Override
        public String parse(String url) throws IOException {
            synchronized(attemptedUrls) {
                attemptedUrls.add(url);
                return url;
            }
        }
    }
}
