package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

public class GroovyLanguage implements LanguageBuilder {
   private static String KEYWORDS = "byte short char int long boolean float double void " +
           // groovy specific keywords
           "def as in assert trait " +
           // common keywords with Java
           "super this abstract static volatile transient public private protected synchronized final " +
           "class interface enum if else for while switch case break default continue " +
           "throw throws try catch finally implements extends new import package return instanceof";
   private static String KEYWORDS_LITERAL = "true false null";

   public Language build() {
      return (Language) new Language()
            .keywords(new Keyword[] {
                    new Keyword("keyword", KEYWORDS),
                    new Keyword("literal", KEYWORDS_LITERAL),
            })
            .contains(new Mode[] {
                  Mode.COMMENT(
                        "/\\*\\*",
                        "\\*/",
                        new Mode().relevance(0).contains(new Mode[] {
                              // eat up @'s in emails to prevent them to be recognized as doctags
                              new Mode().begin("\\w+@").relevance(0),
                              new Mode().className("doctag").begin("@[A-Za-z]+")
                        })
                  ),
                  Mode.C_LINE_COMMENT_MODE,
                  Mode.C_BLOCK_COMMENT_MODE,
                  new Mode()
                        .className("string")
                        .begin("\"\"\"")
                        .end("\"\"\""),
                  new Mode()
                        .className("string")
                        .begin("\'\'\'")
                        .end("\'\'\'"),
                  new Mode()
                        .className("string")
                        .begin("\\$/")
                        .end("/\\$")
                        .relevance(10),
                  Mode.APOS_STRING_MODE,
                  new Mode()
                        .className("regexp")
                        .begin("~?\\/[^\\/\\n]+\\/")
                        .contains(new Mode[] {
                            Mode.BACKSLASH_ESCAPE
                        }),
                  Mode.QUOTE_STRING_MODE,
                  new Mode()
                        .className("meta")
                        .begin("^#!/usr/bin/env").end("$")
                        .illegal("\n"),
                  Mode.BINARY_NUMBER_MODE,
                  new Mode()
                        .className("class")
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "class interface trait enum") })
                        .end("[;{]")
                        .illegal(":")
                        .contains(new Mode[] {
                              new Mode().beginKeywords(new Keyword[] { new Keyword("keyword", "extends implements") }),
                              Mode.UNDERSCORE_TITLE_MODE
                  }),
                  Mode.C_NUMBER_MODE,
                  new Mode().className("meta").begin("@[A-Za-z]+"),
                  // highlight map keys and named parameters as strings
                  new Mode().className("string").begin("[^\\?]{0}[A-Za-z0-9_$]+ *:"),
                  // catch middle element of the ternary operator
                  // to avoid highlight it as a label, named parameter, or map key
                  new Mode().begin("\\?").end("\\:"),
                  // highlight labeled statements
                  new Mode().className("symbol").begin("^\\\\s*[A-Za-z0-9_$]+:").relevance(0)
            })
            .illegal("#|<\\/");
   }
}
