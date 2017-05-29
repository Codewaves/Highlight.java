package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/18/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class CsLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "csharp" };
   private static String KEYWORDS_KEYWORD = "abstract as base bool break byte case catch char checked const continue decimal " +
         "default delegate do double else enum event explicit extern finally fixed float " +
         "for foreach goto if implicit in int interface internal is lock long " +
         "object operator out override params private protected public readonly ref sbyte " +
         "sealed short sizeof stackalloc static string struct switch this try typeof " +
         "uint ulong unchecked unsafe ushort using virtual void volatile while " +
         "nameof " +
         "add alias ascending async await by descending dynamic equals from get global group into join " +
         "let on orderby partial remove select set value var where yield";
   private static String KEYWORDS_LITERAL = "null false true";
   private static Keyword[] KEYWORDS = new Keyword[] {
         new Keyword("keyword", KEYWORDS_KEYWORD),
         new Keyword("literal", KEYWORDS_LITERAL)
   };
   private static String TYPE_IDENT_RE = Mode.IDENT_RE + "(<" + Mode.IDENT_RE + "(\\s*,\\s*" + Mode.IDENT_RE + ")*>)?(\\[\\])?";

   @Override
   public Language build() {
      final Mode VERBATIM_STRING = new Mode().className("string").begin("@\"").end("\"").contains(new Mode[] { new Mode().begin("\"\"") });
      final Mode VERBATIM_STRING_NO_LF = Mode.inherit(VERBATIM_STRING, new Mode().illegal("\\n"));
      final Mode SUBST = new Mode().className("subst").begin("\\{").end("\\}").keywords(KEYWORDS);
      final Mode SUBST_NO_LF = Mode.inherit(SUBST, new Mode().illegal("\\n"));
      final Mode INTERPOLATED_STRING = new Mode()
            .className("string")
            .begin("\\$\"")
            .end("\"")
            .illegal("\\n")
            .contains(new Mode[] {
                  new Mode().begin("\\{\\{"),
                  new Mode().begin("\\}\\}"),
                  Mode.BACKSLASH_ESCAPE,
                  SUBST_NO_LF
            });
      final Mode INTERPOLATED_VERBATIM_STRING = new Mode()
            .className("string")
            .begin("\\$@\"")
            .end("\"")
            .contains(new Mode[] {
                  new Mode().begin("\\{\\{"),
                  new Mode().begin("\\}\\}"),
                  new Mode().begin("\"\""),
                  SUBST
            });
      final Mode INTERPOLATED_VERBATIM_STRING_NO_LF = Mode.inherit(INTERPOLATED_VERBATIM_STRING, new Mode()
            .illegal("\\n")
            .contains(new Mode[] {
                  new Mode().begin("\\{\\{"),
                  new Mode().begin("\\}\\}"),
                  new Mode().begin("\"\""),
                  SUBST_NO_LF
            }));
      final Mode STRING = new Mode().variants(new Mode[] {
            INTERPOLATED_VERBATIM_STRING,
            INTERPOLATED_STRING,
            VERBATIM_STRING,
            Mode.APOS_STRING_MODE,
            Mode.QUOTE_STRING_MODE
      });
      
      SUBST.contains(new Mode[] {
            INTERPOLATED_VERBATIM_STRING,
            INTERPOLATED_STRING,
            VERBATIM_STRING,
            Mode.APOS_STRING_MODE,
            Mode.QUOTE_STRING_MODE,
            Mode.C_NUMBER_MODE,
            Mode.C_BLOCK_COMMENT_MODE
      });
      SUBST_NO_LF.contains(new Mode[] {
            INTERPOLATED_VERBATIM_STRING_NO_LF,
            INTERPOLATED_STRING,
            VERBATIM_STRING_NO_LF,
            Mode.APOS_STRING_MODE,
            Mode.QUOTE_STRING_MODE,
            Mode.C_NUMBER_MODE,
            Mode.inherit(Mode.C_BLOCK_COMMENT_MODE, new Mode().illegal("\\n"))
      });

      return (Language) new Language()
            .aliases(ALIASES)
            .keywords(KEYWORDS)
            .illegal("::")
            .contains(new Mode[] {
                  Mode.COMMENT("///", "$", new Mode()
                        .returnBegin()
                        .contains(new Mode[] {
                              new Mode()
                                    .className("doctag")
                                    .variants(new Mode[] {
                                          new Mode().begin("///").relevance(0),
                                          new Mode().begin("<!--|-->"),
                                          new Mode().begin("</?").end(">")
                              })
                        })),
                  Mode.C_LINE_COMMENT_MODE,
                  Mode.C_BLOCK_COMMENT_MODE,
                  new Mode()
                        .className("meta")
                        .begin("#")
                        .end("$")
                        .keywords(new Keyword[] { new Keyword("meta-keyword", "if else elif endif define undef warning error line region endregion pragma checksum") }),
                  STRING,
                  Mode.C_NUMBER_MODE,
                  new Mode()
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "class interface") })
                        .end("[{;=]")
                        .illegal("[^\\s:]")
                        .contains(new Mode[] { Mode.TITLE_MODE, Mode.C_LINE_COMMENT_MODE, Mode.C_BLOCK_COMMENT_MODE }),
                  new Mode()
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "namespace") })
                        .end("[{;=]")
                        .illegal("[^\\s:]")
                        .contains(new Mode[] { Mode.inherit(Mode.TITLE_MODE, new Mode().begin("[a-zA-Z](\\.?\\w)*")), Mode.C_LINE_COMMENT_MODE, Mode.C_BLOCK_COMMENT_MODE }),
                  new Mode().beginKeywords(new Keyword[] { new Keyword("keyword", "new return throw await") }).relevance(0),
                  new Mode()
                        .className("function")
                        .begin("(" + TYPE_IDENT_RE + "\\s+)+" + Mode.IDENT_RE + "\\s*\\(")
                        .returnBegin()
                        .end("[{;=]")
                        .excludeEnd()
                        .keywords(KEYWORDS)
                        .contains(new Mode[] {
                              new Mode().begin(Mode.IDENT_RE + "\\s*\\(").returnBegin().relevance(0).contains(new Mode[] { Mode.TITLE_MODE }),
                              new Mode()
                                    .className("params")
                                    .begin("\\(")
                                    .end("\\)")
                                    .excludeBegin()
                                    .excludeEnd()
                                    .keywords(KEYWORDS)
                                    .relevance(0)
                                    .contains(new Mode[] {
                                          STRING,
                                          Mode.C_NUMBER_MODE,
                                          Mode.C_BLOCK_COMMENT_MODE
                              }),
                              Mode.C_LINE_COMMENT_MODE,
                              Mode.C_BLOCK_COMMENT_MODE
                        })
            });
   }
}
