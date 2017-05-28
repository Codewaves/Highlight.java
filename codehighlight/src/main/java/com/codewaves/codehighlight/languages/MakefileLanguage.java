package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/20/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class MakefileLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "mk", "mak" };
   private static String KEYWORDS_FUNC = "subst patsubst strip findstring filter filter-out sort " +
         "word wordlist firstword lastword dir notdir suffix basename " +
         "addsuffix addprefix join wildcard realpath abspath error warning " +
         "shell origin flavor foreach if or and call eval file value";
   private static String KEYWORDS = "define endef undefine ifdef ifndef ifeq ifneq else endif " +
         "include -include sinclude override export unexport private vpath";

   @Override
   public Language build() {
      final Mode VARIABLE = new Mode()
            .className("variable")
            .variants(new Mode[] {
                  new Mode().begin("\\$\\(" + Mode.UNDERSCORE_IDENT_RE + "\\)").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                  new Mode().begin("\\$[@%<?\\^\\+\\*]")
            });
      final Mode QUOTE_STRING = new Mode()
            .className("string")
            .begin("\"")
            .end("\"")
            .contains(new Mode[] {
                  Mode.BACKSLASH_ESCAPE,
                  VARIABLE
            });
      final Mode FUNC = new Mode()
            .className("variable")
            .begin("\\$\\([\\w-]+\\s")
            .end("\\)")
            .keywords(new Keyword[] { new Keyword("built_in", KEYWORDS_FUNC) })
            .contains(new Mode[] { VARIABLE });

      final Mode VAR_ASSIG = new Mode()
            .begin("^" + Mode.UNDERSCORE_IDENT_RE + "\\s*[:+?]?=")
            .illegal("\\n")
            .returnBegin()
            .contains(new Mode[] {
                  new Mode().begin("^" + Mode.UNDERSCORE_IDENT_RE).end("[:+?]?=").excludeEnd()
            });
      final Mode META = new Mode()
            .className("meta")
            .begin("^\\.PHONY:")
            .end("$")
            .lexemes("[\\.\\w]+")
            .keywords(new Keyword[] { new Keyword("meta-keyword", ".PHONY") });
      final Mode TARGET = new Mode()
            .className("section")
            .begin("^[^\\s]+:")
            .end("$")
            .contains(new Mode[] { VARIABLE });

      return (Language) new Language()
            .aliases(ALIASES)
            .keywords(new Keyword[] { new Keyword("keyword", KEYWORDS) })
            .lexemes("[\\w-]+")
            .contains(new Mode[] {
                  Mode.HASH_COMMENT_MODE,
                  VARIABLE,
                  QUOTE_STRING,
                  FUNC,
                  VAR_ASSIG,
                  META,
                  TARGET
            });
   }
}
