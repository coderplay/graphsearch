package org.apache.lucene.analysis.ja;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.util.Version;

/**
 * Factory for {@link org.apache.lucene.analysis.ja.JapanesePartOfSpeechStopFilter}.
 * <pre class="prettyprint">
 * &lt;fieldType name="text_ja" class="solr.TextField"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.JapaneseTokenizerFactory"/&gt;
 *     &lt;filter class="solr.JapanesePartOfSpeechStopFilterFactory"
 *             tags="stopTags.txt" 
 *             enablePositionIncrements="true"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;
 * </pre>
 */
public class JapanesePartOfSpeechStopFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
  private final String stopTagFiles;
  private final boolean enablePositionIncrements;
  private Set<String> stopTags;

  /** Creates a new JapanesePartOfSpeechStopFilterFactory */
  public JapanesePartOfSpeechStopFilterFactory(Map<String,String> args) {
    super(args);
    stopTagFiles = get(args, "tags");
    enablePositionIncrements = getBoolean(args, "enablePositionIncrements", true);
    if (enablePositionIncrements == false &&
        (luceneMatchVersion == null || luceneMatchVersion.onOrAfter(Version.LUCENE_4_4_0))) {
      throw new IllegalArgumentException("enablePositionIncrements=false is not supported anymore as of Lucene 4.4");
    }
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }
  
  @Override
  public void inform(ResourceLoader loader) throws IOException {
    stopTags = null;
    CharArraySet cas = getWordSet(loader, stopTagFiles, false);
    if (cas != null) {
      stopTags = new HashSet<>();
      for (Object element : cas) {
        char chars[] = (char[]) element;
        stopTags.add(new String(chars));
      }
    }
  }

  @Override
  public TokenStream create(TokenStream stream) {
    // if stoptags is null, it means the file is empty
    if (stopTags != null) {
      if (luceneMatchVersion == null) {
        return new JapanesePartOfSpeechStopFilter(stream, stopTags);
      }
      @SuppressWarnings("deprecation")
      final TokenStream filter = new JapanesePartOfSpeechStopFilter(luceneMatchVersion, enablePositionIncrements, stream, stopTags);
      return filter;
    } else {
      return stream;
    }
  }
}
