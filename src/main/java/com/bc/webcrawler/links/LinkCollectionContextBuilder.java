package com.bc.webcrawler.links;

import com.bc.webcrawler.predicates.CrawlUrlTest;
import com.bc.webcrawler.util.Buffer;
import com.bc.webcrawler.ContentTypeRequest;
import com.bc.webcrawler.util.Store;

/**
 * @author USER
 */
public interface LinkCollectionContextBuilder<E> {
    
    LinkCollectionContextBuilder<E> attemptedLinkBuffer(Buffer<String> buffer);
    
    LinkCollectionContextBuilder<E> asyncLinkCollection(boolean asyncLinkCollection);

    LinkCollectionContext<E> build();

    LinkCollectionContextBuilder<E> crawlLimit(long crawlLimit);

    LinkCollectionContextBuilder<E> linksExtractor(LinksExtractor<E> linksExtractor);

    LinkCollectionContextBuilder<E> linkStore(Store<String> linkStore);
    
    LinkCollectionContextBuilder<E> crawlUrlTest(CrawlUrlTest urlTest);

    LinkCollectionContextBuilder<E> contentTypeRequest(ContentTypeRequest contentTypeRequest);
}
