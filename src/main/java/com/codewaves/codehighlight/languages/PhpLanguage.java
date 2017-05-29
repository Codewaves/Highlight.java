package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/21/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class PhpLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "php3", "php4", "php5", "php6" };
   private static String KEYWORDS = "and include_once list abstract global private echo interface as static endswitch " +
         "array null if endwhile or const for endforeach self var while isset public " +
         "protected exit foreach throw elseif include __FILE__ empty require_once do xor " +
         "return parent clone use __CLASS__ __LINE__ else break print eval new " +
         "catch __METHOD__ case exception default die require __FUNCTION__ " +
         "enddeclare final try switch continue endfor endif declare unset true false " +
         "trait goto instanceof insteadof __DIR__ __NAMESPACE__ " +
         "yield finally";
   
   @Override
   public Language build() {
      final Mode VARIABLE = new Mode().begin("\\$+[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]");
      final Mode PREPROCESSOR = new Mode().className("meta").begin("<\\?(php)?|\\?>");
      final Mode STRING = new Mode()
            .className("string")
            .contains(new Mode[] { Mode.BACKSLASH_ESCAPE, PREPROCESSOR })
            .variants(new Mode[] {
                  new Mode().begin("b\"").end("\""),
                  new Mode().begin("b\'").end("\'"),
                  new Mode().className("string").begin("\'").end("\'").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                  new Mode().className("string").begin("\"").end("\"").contains(new Mode[] { Mode.BACKSLASH_ESCAPE })
            });
      final Mode NUMBER = new Mode().variants(new Mode[] { Mode.BINARY_NUMBER_MODE, Mode.C_NUMBER_MODE });

      return (Language) new Language()
            .aliases(ALIASES)
            .caseInsensitive()
            .keywords(new Keyword[] { new Keyword("keyword", KEYWORDS) })
            .contains(new Mode[] {
                  Mode.HASH_COMMENT_MODE,
                  Mode.COMMENT("//", "$", new Mode().contains(new Mode[] { PREPROCESSOR })),
                  Mode.COMMENT("/\\*", "\\*/", new Mode().contains(new Mode[] { new Mode().className("doctag").begin("@[A-Za-z]+") })),
                  Mode.COMMENT("__halt_compiler.+?;", "", new Mode()
                        .endsWithParent()
                        .lexemes(Mode.UNDERSCORE_IDENT_RE)
                        .keywords(new Keyword[] { new Keyword("keyword", "__halt_compiler") })),
                  new Mode()
                        .className("string")
                        .begin("<<<['\"]?\\w+['\"]?$")
                        .end("^\\w+;?$")
                        .contains(new Mode[] {
                              Mode.BACKSLASH_ESCAPE,
                              new Mode().className("subst").variants(new Mode[] {
                                    new Mode().begin("\\$\\w+"),
                                    new Mode().begin("\\{\\$").end("\\}")
                              })
                  }),
                  PREPROCESSOR,
                  new Mode().className("keyword").begin("\\$this\\b"),
                  VARIABLE,
                  new Mode().begin("(::|->)+[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*"),
                  new Mode()
                        .className("function")
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "function") })
                        .end("[;{]")
                        .excludeEnd()
                        .illegal("\\$|\\[|%")
                        .contains(new Mode[] {
                              Mode.UNDERSCORE_TITLE_MODE,
                              new Mode()
                                 .className("params")
                                 .begin("\\(")
                                 .end("\\)")
                                 .contains(new Mode[] {
                                    Mode.SELF,
                                    VARIABLE,
                                    Mode.C_BLOCK_COMMENT_MODE,
                                    STRING,
                                    NUMBER
                              })
                  }),
                  new Mode()
                        .className("class")
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "class interface") })
                        .end("\\{")
                        .excludeEnd()
                        .illegal("[:\\(\\$\"]")
                        .contains(new Mode[] {
                              new Mode().beginKeywords(new Keyword[] { new Keyword("keyword", "extends implements") }),
                              Mode.UNDERSCORE_TITLE_MODE
                  }),
                  new Mode()
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "namespace") })
                        .end(";")
                        .illegal("[\\.']")
                        .contains(new Mode[] { Mode.UNDERSCORE_TITLE_MODE }),
                  new Mode()
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "use") })
                        .end(";")
                        .contains(new Mode[] { Mode.UNDERSCORE_TITLE_MODE }),
                  new Mode().begin("=>"),
                  STRING,
                  NUMBER
            });
   }
}
