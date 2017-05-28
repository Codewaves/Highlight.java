package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/22/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class PythonLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "py", "gyp" };
   private static String KEYWORDS = "and elif is global as in if from raise for except finally print import pass return " +
         "exec else break not with class assert yield try while continue del or def lambda " +
         "async await nonlocal|10 None True False";
   private static String KEYWORDS_BUILTIN = "Ellipsis NotImplemented";

   @Override
   public Language build() {
      final Keyword[] PYTHON_KEYWORDS = new Keyword[] {
            new Keyword("keyword", KEYWORDS),
            new Keyword("built_in", KEYWORDS_BUILTIN)
      };
      final Mode PROMPT = new Mode().className("meta").begin("^(>>>|\\.\\.\\.) ");
      final Mode SUBST = new Mode().className("subst").begin("\\{").end("\\}").keywords(PYTHON_KEYWORDS).illegal("#");
      final Mode STRING = new Mode().className("string").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }).variants(new Mode[] {
            new Mode().begin("(u|b)?r?'''").end("'''").contains(new Mode[] { PROMPT }).relevance(10),
            new Mode().begin("(u|b)?r?\"\"\"").end("\"\"\"").contains(new Mode[] { PROMPT }).relevance(10),
            new Mode().begin("(fr|rf|f)'''").end("'''").contains(new Mode[] { PROMPT, SUBST }),
            new Mode().begin("(fr|rf|f)\"\"\"").end("\"\"\"").contains(new Mode[] { PROMPT, SUBST }),
            new Mode().begin("(u|r|ur)'").end("'").relevance(10),
            new Mode().begin("(u|r|ur)\"").end("\"").relevance(10),
            new Mode().begin("(b|br)'").end("'"),
            new Mode().begin("(b|br)\"").end("\""),
            new Mode().begin("(fr|rf|f)'").end("'").contains(new Mode[] { SUBST }),
            new Mode().begin("(fr|rf|f)\"").end("\"").contains(new Mode[] { SUBST }),
            Mode.APOS_STRING_MODE,
            Mode.QUOTE_STRING_MODE
      });
      final Mode NUMBER = new Mode().className("number").relevance(0).variants(new Mode[] {
            new Mode().begin(Mode.BINARY_NUMBER_RE + "[lLjJ]?"),
            new Mode().begin("\\b(0o[0-7]+)[lLjJ]?"),
            new Mode().begin(Mode.C_NUMBER_RE + "[lLjJ]?")
      });
      final Mode PARAMS = new Mode().className("params").begin("\\(").end("\\)").contains(new Mode[] {
            Mode.SELF,
            PROMPT,
            NUMBER,
            STRING
      });
      SUBST.contains(new Mode[] { STRING, NUMBER, PROMPT });

      return (Language) new Language()
            .aliases(ALIASES)
            .keywords(PYTHON_KEYWORDS)
            .illegal("(<\\/|->|\\?)|=>")
            .contains(new Mode[] {
                  PROMPT,
                  NUMBER,
                  STRING,
                  Mode.HASH_COMMENT_MODE,
                  new Mode()
                        .variants(new Mode[] {
                              new Mode().className("function").beginKeywords(new Keyword[] { new Keyword("keyword", "def") }),
                              new Mode().className("class").beginKeywords(new Keyword[] { new Keyword("keyword", "class") })
                        })
                        .end(":")
                        .illegal("[${=;\\n,]")
                        .contains(new Mode[] {
                              Mode.UNDERSCORE_TITLE_MODE,
                              PARAMS,
                              new Mode().begin("->").endsWithParent().keywords(new Keyword[] { new Keyword("keyword", "None") })
                  }),
                  new Mode().className("meta").begin("^[\\t ]*@").end("$"),
                  new Mode().begin("\\b(print|exec)\\(")
            });
   }
}
