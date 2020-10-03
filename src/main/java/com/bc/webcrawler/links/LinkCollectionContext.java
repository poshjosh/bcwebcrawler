package com.bc.webcrawler.links;

import com.bc.webcrawler.predicates.CrawlUrlTest;
import com.bc.webcrawler.Buffer;
import com.bc.webcrawler.ContentTypeRequest;
import com.bc.webcrawler.ResumeHandler;

/**
 * @author USER
 */
public interface LinkCollectionContext<E> {
    
    static <T> LinkCollectionContextBuilder<T> builder(Class<T> type) {
        return new LinkCollectionContextBuilderImpl<>();
    }
    
    default LinkCollector<E> newLinkCollector(String baseUrl, boolean async) {
        final LinkCollector linkCollector = async ? 
                new LinkCollectorAsync(this, baseUrl) :
                new LinkCollectorImpl(this, baseUrl);
        return linkCollector;
    }
    
    boolean isAsyncLinkCollection();

    long getCrawlLimit();
    
    ResumeHandler getResumeHandler();

    default CrawlUrlTest getCrawlUrlTest() {
        return (link) -> true;
    }
    
    ContentTypeRequest getContentTypeRequest();

    LinksExtractor<E> getLinksExtractor();

    Buffer<String> getAttemptedLinkBuffer();
}
