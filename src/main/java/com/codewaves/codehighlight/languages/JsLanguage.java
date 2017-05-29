package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/20/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class JsLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "js", "jsx" };
   private static String IDENT_RE = "[A-Za-z$_][0-9A-Za-z$_]*";
   private static String KEYWORDS = "in of if for while finally var new function do return void else break catch " +
         "instanceof with throw case default try this switch continue typeof delete " +
         "let yield const export super debugger as async await static " +
         "import from as";
   private static String KEYWORDS_BUILTIN = "eval isFinite isNaN parseFloat parseInt decodeURI decodeURIComponent " +
         "encodeURI encodeURIComponent escape unescape Object Function Boolean Error " +
         "EvalError InternalError RangeError ReferenceError StopIteration SyntaxError " +
         "TypeError URIError Number Math Date String RegExp Array Float32Array " +
         "Float64Array Int16Array Int32Array Int8Array Uint16Array Uint32Array " +
         "Uint8Array Uint8ClampedArray ArrayBuffer DataView JSON Intl arguments require " +
         "module console window document Symbol Set Map WeakSet WeakMap Proxy Reflect " +
         "Promise";
   private static String KEYWORDS_LITERAL = "true false null undefined NaN Infinity";

   @Override
   public Language build() {
      final Keyword[] JS_KEYWORDS = new Keyword[] {
            new Keyword("keyword", KEYWORDS),
            new Keyword("built_in", KEYWORDS_BUILTIN),
            new Keyword("literal", KEYWORDS_LITERAL)
      };
      final Mode EXPRESSIONS;
      final Mode NUMBER = new Mode().className("number").relevance(0).variants(new Mode[] {
            new Mode().begin("\\b(0[bB][01]+)"),
            new Mode().begin("\\b(0[oO][0-7]+)"),
            new Mode().begin(Mode.C_NUMBER_RE)
      });
      final Mode SUBST = new Mode().className("subst").begin("\\$\\{").end("\\}").keywords(JS_KEYWORDS).contains(new Mode[] { });
      final Mode TEMPLATE_STRING = new Mode().className("string").begin("`").end("`").contains(new Mode[] {
            Mode.BACKSLASH_ESCAPE,
            SUBST
      });
      SUBST.contains(new Mode[] {
            Mode.APOS_STRING_MODE,
            Mode.QUOTE_STRING_MODE,
            TEMPLATE_STRING,
            NUMBER,
            Mode.REGEXP_MODE
      });
      final Mode[] PARAMS_CONTAINS = Mode.mergeModes(SUBST.getContains(), new Mode[] {
            Mode.C_BLOCK_COMMENT_MODE,
            Mode.C_LINE_COMMENT_MODE
      });

      return (Language) new Language()
            .aliases(ALIASES)
            .keywords(JS_KEYWORDS)
            .illegal("#(?!!)")
            .contains(new Mode[] {
                  new Mode().className("meta").relevance(10).begin("^\\s*[\'\"]use (strict|asm)[\'\"]"),
                  new Mode().className("meta").begin("^#!").end("$"),
                  Mode.APOS_STRING_MODE,
                  Mode.QUOTE_STRING_MODE,
                  TEMPLATE_STRING,
                  Mode.C_LINE_COMMENT_MODE,
                  Mode.C_BLOCK_COMMENT_MODE,
                  NUMBER,
                  new Mode().begin("[\\{,]\\s*").relevance(0).contains(new Mode[] {
                        new Mode().begin(IDENT_RE + "\\s*:").returnBegin().relevance(0).contains(new Mode[] {
                              new Mode().className("attr").begin(IDENT_RE).relevance(0)
                        })
                  }),
                  new Mode().begin("(" + Mode.RE_STARTERS_RE + "|\\b(case|return|throw)\\b)\\s*")
                        .relevance(0)
                        .keywords(new Keyword[] { new Keyword("keyword", "return throw case") })
                        .contains(new Mode[] {
                              Mode.C_LINE_COMMENT_MODE,
                              Mode.C_BLOCK_COMMENT_MODE,
                              Mode.REGEXP_MODE,
                              new Mode().className("function")
                                    .begin("(\\(.*?\\)|" + IDENT_RE + ")\\s*=>")
                                    .returnBegin()
                                    .end("\\s*=>")
                                    .contains(new Mode[] {
                                          new Mode().className("params").variants(new Mode[] {
                                                new Mode().begin(IDENT_RE),
                                                new Mode().begin("\\(\\s*\\)"),
                                                new Mode().begin("\\(").end("\\)").excludeBegin().excludeEnd().keywords(JS_KEYWORDS).contains(PARAMS_CONTAINS)
                                          })
                              }),
                              new Mode().begin("<").end("(\\/\\w+|\\w+\\/)>").subLanguage("xml").contains(new Mode[] {
                                    new Mode().begin("<\\w+\\s*\\/>").skip(),
                                    new Mode().begin("<\\w+").end("(\\/\\w+|\\w+\\/)>").skip().contains(new Mode[] {
                                          new Mode().begin("<\\w+\\s*\\/>").skip(),
                                          Mode.SELF
                                    })
                              })
                        }),
                  new Mode()
                        .className("function")
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "function") })
                        .end("\\{")
                        .excludeEnd()
                        .illegal("\\[|%")
                        .contains(new Mode[] {
                              Mode.inherit(Mode.TITLE_MODE, new Mode().begin(IDENT_RE)),
                              new Mode().className("params").begin("\\(").end("\\)").excludeBegin().excludeEnd().contains(PARAMS_CONTAINS)
                  }),
                  new Mode().begin("\\$[(.]"),
                  Mode.METHOD_GUARD,
                  new Mode()
                        .className("class")
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "class") })
                        .end("[{;=]")
                        .excludeEnd()
                        .illegal("[:\"\\[\\]]")
                        .contains(new Mode[] {
                              new Mode().beginKeywords(new Keyword[] { new Keyword("keyword", "extends") }),
                              Mode.UNDERSCORE_TITLE_MODE
                  }),
                  new Mode().beginKeywords(new Keyword[] { new Keyword("keyword", "constructor") }).end("\\{").excludeEnd()
            });
   }
}
