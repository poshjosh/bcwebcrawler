package com.bc.webcrawler.links;

import com.bc.webcrawler.Buffer;
import com.bc.webcrawler.ContentTypeRequest;
import com.bc.webcrawler.ResumeHandler;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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

    default Predicate<String> getCrawlUrlTest() {
        return (link) -> true;
    }
    
    ContentTypeRequest getContentTypeRequest();

    Function<E, Set<String>> getLinksExtractor();

    Buffer<String> getAttemptedLinkBuffer();
}
