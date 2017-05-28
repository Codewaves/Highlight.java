package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/19/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class IniLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "toml" };
   @Override
   public Language build() {
      final Mode STRING = new Mode()
            .className("string")
            .contains(new Mode[] { Mode.BACKSLASH_ESCAPE })
            .variants(new Mode[] {
                  new Mode().begin("'''").end("'''").relevance(10),
                  new Mode().begin("\"\"\"").end("\"\"\"").relevance(10),
                  new Mode().begin("\"").end("\""),
                  new Mode().begin("'").end("'")
            });

      return (Language) new Language()
            .aliases(ALIASES)
            .caseInsensitive()
            .illegal("\\S")
            .contains(new Mode[] {
                  Mode.COMMENT(";", "$", null),
                  Mode.HASH_COMMENT_MODE,
                  new Mode().className("section").begin("^\\s*\\[+").end("\\]+"),
                  new Mode()
                        .begin("^[a-z0-9\\[\\]_-]+\\s*=\\s*")
                        .end("$")
                        .returnBegin()
                        .contains(new Mode[] {
                              new Mode().className("attr").begin("[a-z0-9\\[\\]_-]+"),
                              new Mode()
                                    .begin("=")
                                    .endsWithParent()
                                    .relevance(0)
                                    .contains(new Mode[] {
                                          new Mode().className("literal").begin("\\bon|off|true|false|yes|no\\b"),
                                          new Mode().className("variable").variants(new Mode[] {
                                                new Mode().begin("\\$[\\w\\d\"][\\w\\d_]*"),
                                                new Mode().begin("\\$\\{(.*?)\\}")
                                          }),
                                          STRING,
                                          new Mode().className("number").begin("([\\+\\-]+)?[\\d]+_[\\d_]+"),
                                          Mode.NUMBER_MODE
                              })
                  })
            });
   }
}
