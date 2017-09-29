package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

public class ScalaLanguage implements LanguageBuilder {
    private static String KEYWORDS_LITERAL = "true false null";
    private static String KEYWORDS = "type yield lazy override def with val " +
           "var sealed abstract private trait object if forSome for while " +
           "throw finally protected extends import final return else break " +
           "new catch super class case package default try this match " +
           "continue throws implicit";

   public Language build() {
       final Mode ANNOTATION = new Mode().className("meta").begin("@[A-Za-z]+");

       // used in strings for escaping/interpolation/substitution
       final Mode SUBST = new Mode()
               .className("subst")
               .variants(new Mode[] {
                       new Mode().begin("\\$[A-Za-z0-9_]+"),
                       new Mode().begin("\\$\\{").end("\\}")
               });

       final Mode STRINGS = new Mode()
               .className("string")
               .variants(new Mode[] {
                       new Mode().begin("\"").end("\"")
                               .illegal("\\n")
                               .contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                       new Mode().begin("\"\"\"").end("\"\"\"").relevance(10),
                       new Mode().begin("[a-z]+\"").end("\"")
                               .illegal("\\n")
                               .contains(new Mode[] { Mode.BACKSLASH_ESCAPE, SUBST }),
                       new Mode()
                               .className("string").begin("[a-z]+\"\"\"").end("\"\"\"")
                               .contains(new Mode[] { SUBST }).relevance(10)
               });

       final Mode SYMBOL = new Mode().className("symbol").begin("\'\\w[\\w\\d_]*(?!\')");

       final Mode TYPE = new Mode().className("type").begin("\\b[A-Z][A-Za-z0-9_]*").relevance(0);

       final Mode NAME = new Mode().className("title")
               .begin("[^0-9\\n\\t \"'(),.`{}\\[\\]:;][^\\n\\t \"'(),.`{}\\[\\]:;]+|[^0-9\\n\\t \"'(),.`{}\\[\\]:;=]")
               .relevance(0);

       final Mode CLASS = new Mode().className("class")
               .beginKeywords(new Keyword[] { new Keyword("keyword", "class object trait type") })
               .end("[:={\\[\\n;]").excludeEnd()
               .contains(new Mode[] {
                       new Mode()
                               .beginKeywords(new Keyword[] { new Keyword("keyword", "extends with") })
                               .relevance(10),
                       new Mode().begin("\\[").end("\\]").excludeBegin().excludeEnd().relevance(0)
                               .contains(new Mode[] { TYPE }),
                       new Mode().className("params").begin("\\(").end("\\)").excludeBegin().excludeEnd().relevance(0)
                               .contains(new Mode[] { TYPE }),
                       NAME
               });
       final Mode METHOD = new Mode().className("function")
               .beginKeywords(new Keyword[] { new Keyword("keyword", "def")})
               .end("[:={\\[(\\n;]").excludeEnd().contains(new Mode[] { NAME });

      return (Language) new Language()
            .keywords(new Keyword[] {
                    new Keyword("literal", KEYWORDS_LITERAL),
                    new Keyword("keyword", KEYWORDS)
            })
            .contains(new Mode[] {
                    Mode.C_LINE_COMMENT_MODE,
                    Mode.C_BLOCK_COMMENT_MODE,
                    STRINGS,
                    SYMBOL,
                    TYPE,
                    METHOD,
                    CLASS,
                    Mode.C_NUMBER_MODE,
                    ANNOTATION
            });
   }
}
