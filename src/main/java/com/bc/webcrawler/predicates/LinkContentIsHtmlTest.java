package com.bc.webcrawler.predicates;

import java.util.Arrays;
import java.util.HashSet;
import com.bc.webcrawler.ConnectionProvider;

public class LinkContentIsHtmlTest extends LinkContentTest {
    
    public LinkContentIsHtmlTest(
            ConnectionProvider connectionProvider,
            int connectTimeout, int readTimeout) {
        super(
                connectionProvider, 
                new HashSet(Arrays.asList(".html", ".php", ".htm", ".xhtml", ".asp", ".aspx", ".jsp", ".jspx", ".xml")), 
                "html", connectTimeout, readTimeout);
    }
}