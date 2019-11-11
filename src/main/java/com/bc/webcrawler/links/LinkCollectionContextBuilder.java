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
public interface LinkCollectionContextBuilder<E> {
    
    LinkCollectionContextBuilder<E> attemptedLinkBuffer(Buffer<String> buffer);
    
    LinkCollectionContextBuilder<E> asyncLinkCollection(boolean asyncLinkCollection);

    LinkCollectionContext<E> build();

    LinkCollectionContextBuilder<E> crawlLimit(long crawlLimit);

    LinkCollectionContextBuilder<E> linksExtractor(Function<E, Set<String>> linksExtractor);

    LinkCollectionContextBuilder<E> resumeHandler(ResumeHandler resumeHandler);
    
    LinkCollectionContextBuilder<E> crawlUrlTest(Predicate<String> urlTest);

    LinkCollectionContextBuilder<E> contentTypeRequest(ContentTypeRequest contentTypeRequest);
}
