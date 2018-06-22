/*
 * Copyright 2017 NUROX Ltd.
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

import java.util.Arrays;
import java.util.HashSet;
import com.bc.webcrawler.ConnectionProvider;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2017 7:04:47 PM
 */
public class LinkContentIsImageTest extends LinkContentTest {

    public LinkContentIsImageTest(
            ConnectionProvider connectionProvider,
            int connectTimeout, int readTimeout) {
        super(
                connectionProvider, 
                new HashSet(Arrays.asList("jpeg", "jpg", "gif", "png", "bnp", "webp")), 
                "image", connectTimeout, readTimeout);
    }
}
