package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/16/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class ApacheLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "apacheconf" };
   private static String KEYWORDS = "order deny allow setenv rewriterule rewriteengine rewritecond documentroot " +
         "sethandler errordocument loadmodule options header listen serverroot servername";

   @Override
   public Language build() {
      final Mode NUMBER = new Mode()
            .className("number")
            .begin("[\\$%]\\d+");

      return (Language) new Language()
            .aliases(ALIASES)
            .caseInsensitive()
            .illegal("\\S")
            .contains(new Mode[] {
                  Mode.HASH_COMMENT_MODE,
                  new Mode()
                        .className("section")
                        .begin("</?")
                        .end(">"),
                  new Mode()
                        .className("attribute")
                        .begin("\\w+")
                        .relevance(0)
                        .keywords(new Keyword[] { new Keyword("nomarkup", KEYWORDS) })
                  .starts(new Mode()
                        .end("$")
                        .relevance(0)
                        .keywords(new Keyword[] { new Keyword("literal", "on off all") })
                        .contains(new Mode[] {
                              new Mode().className("meta").begin("\\s\\[").end("\\]$"),
                              new Mode().className("variable").begin("[\\$%]\\{").end("\\}").contains(new Mode[] {
                                    Mode.SELF,
                                    NUMBER
                              }),
                              NUMBER,
                              Mode.QUOTE_STRING_MODE
                        })
                  )
            });
   }
}
