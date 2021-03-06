## Web Crawler Api

* Elegantly expressive abstraction of simple web crawling and data extraction logic.
* Abstracts easily over any html parser libraries like Jsoup, htmlparser etc
* Light weight (30 classes)
* API itself has no dependencies. To use it however you choose your own html parser
implementation and plug-in.

### Format

```java
        // Configure CrawlerContext
        //
        Function<Document, Set<String>> linkExtractor;

        com.bc.webcrawler.UrlParser<Document> urlParser;

        final CrawlerContext<Document> context = CrawlerContext.builder(Document.class)
                .crawlLimit(1000)
                .crawlUrlTest((link) -> true)
                .parseLimit(100)
                .parseUrlTest((link) -> true)
                .linksExtractor(linkExtractor)
                .urlParser(urlParser)
                .build();
          
        String startUrl;
        
        // Create the Crawler 
        //
        final Crawler<Document> crawler = context.newCrawler(Collections.singleton(startUrl));

        // Use the crawler
        //
        try{

            while(crawler.hasNext()) {

                final Document doc = crawler.next();

                final CrawlMetaData metaData = crawler.getMetaData();

                System.out.println(MessageFormat.format(
                        "Attempted: {0}, failed: {1}", 
                        metaData.getAttempted(), metaData.getFailed()));
            }   
        }finally{
            crawler.shutdown();
        }
```

### Use case with Jsoup

```java

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

        private final Map<String, String> cookies;

        public JsoupUrlParser(int timeout, int maxBodySize) {
            this.timeout = timeout;
            this.maxBodySize = maxBodySize;
            this.cookies = new HashMap<>();
        }


        @Override
        public Document parse(String link) throws MalformedURLException, IOException {

            final URL url = new URL(link);

            final Connection.Response res = HttpConnection
                    .connect(url)
                    .followRedirects(true)
                    .cookies(cookies)
                    .timeout(timeout)
                    .maxBodySize(maxBodySize)
                    .execute();

            collectCookies(res);

            final Document doc = res.parse();

            return doc;
        }

        private void collectCookies(Connection.Response res) {

            final Map<String, String> cookiesFromRes = res.cookies();

            logger.finer(() -> "Cookies from response: " + cookiesFromRes);

            this.cookies.putAll(cookiesFromRes);
        }

        @Override
        public Map<String, String> getCookies() {
            return new LinkedHashMap(cookies);
        }
    }
    
    public static void main(String... args) throws IOException {
        
        final int connectTimeout = 10_000;
        final int readTimeout = 20_000;
        final int maxBodySize = 100_000_000;
        
        final UrlParser<Document> parser;
        
        parser = new JsoupUrlParser(connectTimeout + readTimeout, maxBodySize);
        
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
                .retryOnExceptionTestSupplier(() -> (exception) -> false) 
                .timeoutMillis(3600_000 * n)
                .urlFormatter((link) -> link)
                .urlParser(parser)
                .build();
                
        final Crawler<Document> crawler = context.newCrawler(Collections.singleton(startUrl));

        try{

            while(crawler.hasNext()) {

                final Document doc = crawler.next();

                final CrawlMetaData metaData = crawler.getMetaData();

                System.out.println(MessageFormat.format(
                        "Attempted: {0}, failed: {1}", 
                        metaData.getAttempted(), metaData.getFailed()));

                if(doc == null) {
                    System.err.println("Failed: " + crawler.getCurrentUrl());
                    continue;
                }

                final String url = doc.location();

                System.out.println("URL: " + url +  "\nTitle: " + doc.title());

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
    }
}

```

The above code depends on:
```xml
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.11.2</version>
            <scope>test</scope>
        </dependency>
```
            