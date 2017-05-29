package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/12/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class JavaLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "jsp" };
   private static String JAVA_IDENT_RE = "[\u00C0-\u02B8a-zA-Z_$][\u00C0-\u02B8a-zA-Z_$0-9]*";
   private static String GENERIC_IDENT_RE = JAVA_IDENT_RE + "(<" + JAVA_IDENT_RE + "(\\s*,\\s*" + JAVA_IDENT_RE + ")*>)?";
   private static String KEYWORDS = "false synchronized int abstract float private char boolean static null if const " +
         "for true while long strictfp finally protected import native final void " +
         "enum else break transient catch instanceof byte super volatile case assert short " +
         "package default double public try this switch continue throws protected public private " +
         "module requires exports do";
   private static String JAVA_NUMBER_RE = "\\b" +
         "(" +
         "0[bB]([01]+[01_]+[01]+|[01]+)" + // 0b...
         "|" +
         "0[xX]([a-fA-F0-9]+[a-fA-F0-9_]+[a-fA-F0-9]+|[a-fA-F0-9]+)" + // 0x...
         "|" +
         "(" +
         "([\\d]+[\\d_]+[\\d]+|[\\d]+)(\\.([\\d]+[\\d_]+[\\d]+|[\\d]+))?" +
         "|" +
         "\\.([\\d]+[\\d_]+[\\d]+|[\\d]+)" +
         ")" +
         "([eE][-+]?\\d+)?" + // octal, decimal, float
         ")" +
         "[lLfF]?";

   public Language build() {
      final Mode JAVA_NUMBER_MODE = new Mode()
            .className("number")
            .begin(JAVA_NUMBER_RE)
            .relevance(0);

      return (Language) new Language()
            .aliases(ALIASES)
            .keywords(new Keyword[] { new Keyword("keyword", KEYWORDS) })
            .illegal("<\\/|#")
            .contains(new Mode[] {
                  Mode.COMMENT(
                        "/\\*\\*",
                        "\\*/",
                        new Mode().relevance(0).contains(new Mode[] {
                              new Mode().begin("\\w+@").relevance(0),
                              new Mode().className("doctag").begin("@[A-Za-z]+")
                        })
                  ),
                  Mode.C_LINE_COMMENT_MODE,
                  Mode.C_BLOCK_COMMENT_MODE,
                  Mode.APOS_STRING_MODE,
                  Mode.QUOTE_STRING_MODE,
                  new Mode()
                        .className("class")
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "class interface") })
                        .end("[{;=]")
                        .excludeEnd()
                        .keywords(new Keyword[] { new Keyword("keyword", "class interface") })
                        .illegal("[:\"\\[\\]]")
                        .contains(new Mode[] {
                              new Mode().beginKeywords(new Keyword[] { new Keyword("keyword", "extends implements") }),
                              Mode.UNDERSCORE_TITLE_MODE
                  }),
                  new Mode().beginKeywords(new Keyword[] { new Keyword("keyword", "new throw return else") }).relevance(0),
                  new Mode()
                        .className("function")
                        .begin("(" + GENERIC_IDENT_RE + "\\s+)+" + Mode.UNDERSCORE_IDENT_RE + "\\s*\\(")
                        .end("[{;=]")
                        .returnBegin()
                        .excludeEnd()
                        .keywords(new Keyword[] { new Keyword("keyword", KEYWORDS) })
                        .contains(new Mode[] {
                              new Mode().begin(Mode.UNDERSCORE_IDENT_RE + "\\s*\\(").returnBegin().relevance(0)
                                    .contains(new Mode[] { Mode.UNDERSCORE_TITLE_MODE }),
                              new Mode().className("params").begin("\\(").end("\\)").keywords(new Keyword[] { new Keyword("keyword", KEYWORDS) }).relevance(0)
                                    .contains(new Mode[] { Mode.APOS_STRING_MODE, Mode.QUOTE_STRING_MODE, Mode.C_NUMBER_MODE, Mode.C_BLOCK_COMMENT_MODE}),
                              Mode.C_LINE_COMMENT_MODE,
                              Mode.C_BLOCK_COMMENT_MODE
                  }),
                  JAVA_NUMBER_MODE,
                  new Mode().className("meta").begin("@[A-Za-z]+")
            });
   }
}
