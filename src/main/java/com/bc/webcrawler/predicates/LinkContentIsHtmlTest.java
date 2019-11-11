package com.bc.webcrawler.predicates;

import java.util.HashSet;
import com.bc.webcrawler.ContentTypeRequest;

public class LinkContentIsHtmlTest extends LinkContentTest {
    
    public LinkContentIsHtmlTest(
            ContentTypeRequest contentTypeProvider,
            int connectTimeout, int readTimeout,
            boolean resultIfNone) {
        super(
                contentTypeProvider, 
                new HashSet(new Extensions().forMulti(Extensions.HTML, Extensions.SERVER_PAGE)), 
                new HashSet(new Extensions().forMulti(Extensions.IMAGE, Extensions.AUDIO, 
                        Extensions.VIDEO, Extensions.DOCUMENT)), 
                "html", connectTimeout, readTimeout, resultIfNone);
    }
}