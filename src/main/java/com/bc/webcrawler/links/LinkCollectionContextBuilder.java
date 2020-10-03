package com.bc.webcrawler.links;

import com.bc.webcrawler.predicates.CrawlUrlTest;
import com.bc.webcrawler.Buffer;
import com.bc.webcrawler.ContentTypeRequest;
import com.bc.webcrawler.ResumeHandler;

/**
 * @author USER
 */
public interface LinkCollectionContextBuilder<E> {
    
    LinkCollectionContextBuilder<E> attemptedLinkBuffer(Buffer<String> buffer);
    
    LinkCollectionContextBuilder<E> asyncLinkCollection(boolean asyncLinkCollection);

    LinkCollectionContext<E> build();

    LinkCollectionContextBuilder<E> crawlLimit(long crawlLimit);

    LinkCollectionContextBuilder<E> linksExtractor(LinksExtractor<E> linksExtractor);

    LinkCollectionContextBuilder<E> resumeHandler(ResumeHandler resumeHandler);
    
    LinkCollectionContextBuilder<E> crawlUrlTest(CrawlUrlTest urlTest);

    LinkCollectionContextBuilder<E> contentTypeRequest(ContentTypeRequest contentTypeRequest);
}
