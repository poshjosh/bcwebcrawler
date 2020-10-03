package com.bc.webcrawler.links;

import com.bc.webcrawler.predicates.CrawlUrlTest;
import com.bc.webcrawler.Buffer;
import com.bc.webcrawler.BufferInMemoryStore;
import com.bc.webcrawler.ContentTypeRequest;
import com.bc.webcrawler.ContentTypeRequestImpl;
import com.bc.webcrawler.CrawlerContext;
import com.bc.webcrawler.ResumeHandler;
import com.bc.webcrawler.ResumeHandlerInMemoryStore;
import com.bc.webcrawler.predicates.HtmlLinkIsToBeCrawledTest;

/**
 * @author USER
 */
public class LinkCollectionContextBuilderImpl<E> 
        implements LinkCollectionContext<E>, LinkCollectionContextBuilder<E> {

//    private static final Logger LOG = Logger.getLogger(LinkCollectionContextBuilderImpl.class.getName());

    private boolean buildAttempted;
    
    private final boolean crawlQueryUrls = true;
    
    private final int connectTimeoutForLinkValidation = 5_000;
    private final int readTimeoutForLinkValidation = 10_000;
    
    private boolean asyncLinkCollection = true;
    
    private long crawlLimit = Long.MAX_VALUE;
    
    private CrawlUrlTest crawlUrlTest;

    private LinksExtractor<E> linksExtractor;

    private Buffer<String> attemptedLinkBuffer;
    
    private ResumeHandler resumeHandler; 
    
    private ContentTypeRequest contentTypeRequest;
    
    public LinkCollectionContextBuilderImpl() {  }
    
    @Override
    public LinkCollectionContext build() {
        
        this.checkBuildAttempted();
        
        this.buildAttempted = true;

        if(this.resumeHandler == null) {
            this.resumeHandler = new ResumeHandlerInMemoryStore();
        }
        
        if(this.attemptedLinkBuffer == null){
            this.attemptedLinkBuffer = new BufferInMemoryStore();
        }
        
        if(this.crawlUrlTest == null) {
            
            if(contentTypeRequest == null) {
                contentTypeRequest = new ContentTypeRequestImpl();
            }
            
            this.crawlUrlTest = new HtmlLinkIsToBeCrawledTest(
                    contentTypeRequest,
                    connectTimeoutForLinkValidation, 
                    readTimeoutForLinkValidation, 
                    crawlQueryUrls
            );
        }
        
        return this;
    }

    private void checkBuildAttempted() {
        if(this.buildAttempted) {
            throw new IllegalStateException("build() method may only be called once");
        }
    }

    @Override
    public boolean isAsyncLinkCollection() {
        return asyncLinkCollection;
    }

    @Override
    public LinkCollectionContextBuilder<E> asyncLinkCollection(boolean asyncLinkCollection) {
        this.asyncLinkCollection = asyncLinkCollection;
        return this;
    }

    @Override
    public CrawlUrlTest getCrawlUrlTest() {
        return crawlUrlTest;
    }

    @Override
    public LinkCollectionContextBuilder<E> crawlUrlTest(CrawlUrlTest urlTest) {
        this.crawlUrlTest = urlTest;
        return this;
    }

    @Override
    public LinksExtractor<E> getLinksExtractor() {
        return linksExtractor;
    }

    @Override
    public LinkCollectionContextBuilder<E> linksExtractor(LinksExtractor<E> linksExtractor) {
        this.linksExtractor = linksExtractor;
        return this;
    }

    @Override
    public Buffer<String> getAttemptedLinkBuffer() {
        return attemptedLinkBuffer;
    }

    @Override
    public LinkCollectionContextBuilder<E> attemptedLinkBuffer(Buffer<String> attemptedLinkBuffer) {
        this.attemptedLinkBuffer = attemptedLinkBuffer;
        return this;
    }

    @Override
    public ResumeHandler getResumeHandler() {
        return resumeHandler;
    }

    @Override
    public LinkCollectionContextBuilder<E> resumeHandler(ResumeHandler resumeHandler) {
        this.resumeHandler = resumeHandler;
        return this;
    }

    @Override
    public long getCrawlLimit() {
        return crawlLimit;
    }

    @Override
    public LinkCollectionContextBuilder<E> crawlLimit(long crawlLimit) {
        this.crawlLimit = crawlLimit;
        return this;
    }

    @Override
    public ContentTypeRequest getContentTypeRequest() {
        return contentTypeRequest;
    }

    @Override
    public LinkCollectionContextBuilder<E> contentTypeRequest(ContentTypeRequest connProvider) {
        this.contentTypeRequest = connProvider;
        return this;
    }


    @Override
    public String toString() {
        return CrawlerContext.class.getName() + '@' + Integer.toHexString(hashCode()) + 
                "{crawlQueryUrls=" + crawlQueryUrls + ", crawlLimit=" + crawlLimit + 
                ", connectTimeoutForLinkValidation=" + connectTimeoutForLinkValidation + 
                ", readTimeoutForLinkValidation=" + readTimeoutForLinkValidation + 
                '}';
    }
}

