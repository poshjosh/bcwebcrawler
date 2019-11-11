/*
 * Copyright 2018 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.webcrawler.predicates;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 28, 2018 9:38:21 AM
 */
public class Extensions implements Serializable {
    
    public static final String HTML = "html";
    public static final String SERVER_PAGE = "serverpage";
    public static final String IMAGE = "image";
    public static final String AUDIO = "audio";
    public static final String VIDEO = "video";
    public static final String DOCUMENT = "document";
    
    public Collection<String> forMulti(String... extList) {
        final Set<String> output = new HashSet<>();
        for(String ext : extList) {
            output.addAll(this.forName(ext));
        }
        return Collections.unmodifiableSet(output);
    }
    public Collection<String> forName(String ext) {
        final Collection<String> output;
        switch(ext) {
            case HTML: output = forHtml(); break;
            case SERVER_PAGE: output = forServerPages(); break;
            case IMAGE: output = forImages(); break;
            case AUDIO: output = forAudio(); break;
            case VIDEO: output = forVideos(); break;
            case DOCUMENT: output = forDocuments(); break;
            default: throw new IllegalArgumentException(ext);
        }
        return output;
    }
    public Collection<String> forHtml() {
        return Arrays.asList(".html", ".htm", ".xhtml");
    }
    public Collection<String> forServerPages() {
        return Arrays.asList(".php", ".asp", ".aspx", ".jsp", ".jspx");
    }
    public Collection<String> forImages() {
        return Arrays.asList(".jpeg", ".jpg", ".gif", ".png", ".bnp", ".webp", ".tiff");
    }
    public Collection<String> forAudio() {
        return Arrays.asList(".mp3");
    }
    public Collection<String> forVideos() {
        return Arrays.asList(".mpeg", ".mp4", ".3gpp", ".wmv", ".wav");
    }
    public Collection<String> forDocuments() {
        return Arrays.asList(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx");
    }
}
