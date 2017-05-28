package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/18/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class CssLanguage implements LanguageBuilder {
   private static String IDENT_RE = "[a-zA-Z-][a-zA-Z0-9_-]*";

   @Override
   public Language build() {
      final Mode RULE = new Mode()
            .begin("[A-Z\\_\\.\\-]+\\s*:")
            .returnBegin()
            .end(";")
            .endsWithParent()
            .contains(new Mode[] {
                  new Mode()
                        .className("attribute")
                        .begin("\\S")
                        .end(":")
                        .excludeEnd()
                        .starts(new Mode()
                              .endsWithParent()
                              .excludeEnd()
                              .contains(new Mode[] {
                                    new Mode()
                                          .begin("[\\w-]+\\(")
                                          .returnBegin()
                                          .contains(new Mode[] {
                                                new Mode().className("built_in").begin("[\\w-]+"),
                                                new Mode().begin("\\(").end("\\)").contains(new Mode[] {
                                                      Mode.APOS_STRING_MODE,
                                                      Mode.QUOTE_STRING_MODE
                                                })
                                    }),
                                    Mode.CSS_NUMBER_MODE,
                                    Mode.QUOTE_STRING_MODE,
                                    Mode.APOS_STRING_MODE,
                                    Mode.C_BLOCK_COMMENT_MODE,
                                    new Mode().className("number").begin("#[0-9A-Fa-f]+"),
                                    new Mode().className("meta").begin("!important")
                              })
                  )
            });

      return (Language) new Language()
            .caseInsensitive()
            .illegal("[=\\/|'\\$]")
            .contains(new Mode[] {
                  Mode.C_BLOCK_COMMENT_MODE,
                  new Mode().className("selector-id").begin("#[A-Za-z0-9_-]+"),
                  new Mode().className("selector-class").begin("\\.[A-Za-z0-9_-]+"),
                  new Mode().className("selector-attr").begin("\\[").end("\\]").illegal("$"),
                  new Mode().className("selector-pseudo").begin(":(:)?[a-zA-Z0-9\\_\\-\\+\\(\\)\"'.]+"),
                  new Mode().begin("@(font-face|page)").lexemes("[a-z-]+").keywords(new Keyword[] { new Keyword("keyword", "font-face page") }),
                  new Mode()
                        .begin("@")
                        .end("[\\{;]")
                        .illegal(":")
                        .contains(new Mode[] {
                              new Mode().className("keyword").begin("\\w+"),
                              new Mode()
                                    .begin("\\s")
                                    .excludeEnd()
                                    .endsWithParent()
                                    .relevance(0)
                                    .contains(new Mode[] {
                                          Mode.APOS_STRING_MODE,
                                          Mode.QUOTE_STRING_MODE,
                                          Mode.CSS_NUMBER_MODE
                                    })
                  }),
                  new Mode().className("selector-tag").begin(IDENT_RE).relevance(0),
                  new Mode().begin("\\{").end("\\}").illegal("\\S").contains(new Mode[] { Mode.C_BLOCK_COMMENT_MODE, RULE })
            });
   }
}
