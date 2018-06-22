package com.bc.webcrawler;

import com.bc.net.RetryConnectionFilter;
import com.bc.webcrawler.predicates.HtmlLinkIsToBeCrawledTest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class ReadMeDOMCrawlerTest {
    
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
                if(node.startTag == HTML.Tag.A) {
                    final Object href = node.attributes.getAttribute(HTML.Attribute.HREF);
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
            try(InputStream in = url.openStream();
                    Reader r = new InputStreamReader(in, "utf-8")) {
                this.parserDelegator.parse(r, this.parserCallback, true);
            }
            return this.parserCallback.getNodeList();
        }
        @Override
        public Map<String, String> getCookies() {
            return Collections.EMPTY_MAP;
        }
    }

    @Test
    public void test() {
        
        new ReadMeDOMCrawlerTest().crawl("http://www.looseboxes.com");
    }
    
    public void crawl(String startUrl) {
        
        final UrlParser<List<Node>> urlParser = new UrlParserImpl();
        
        final ConnectionProvider urlConnProvider = 
                new ConnectionProviderImpl(() -> urlParser.getCookieList());
    
        final Predicate<String> crawlHtmlLinks = new HtmlLinkIsToBeCrawledTest(
                urlConnProvider, urlParser, 7_000, 7_000, true);
        
        final CrawlerContext<List> crawlerContext = CrawlerContext.builder(List.class)
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
                .preferredLinkTest((url) -> false)
                .resumeHandler(new ResumeHandlerInMemoryCache(Collections.EMPTY_SET))
                .retryOnExceptionTestSupplier(() -> new RetryConnectionFilter(2, 2_000L))
                .timeoutMillis(7_000)
                .urlFormatter((url) -> url)
                .urlParser(urlParser)
                .build();

        final Crawler<List> crawler = crawlerContext.newCrawler(Collections.singleton(startUrl));    
        
        crawler.stream().forEach((nodeList) -> {
            System.out.println("\nPrinting Nodes");
            String title = null;
            for(Object obj : nodeList) {
                System.out.println(obj);
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
    }
}
