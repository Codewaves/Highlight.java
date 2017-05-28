package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/18/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class CppLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "c", "cc", "h", "c++", "h++", "hpp" };
   private static String FUNCTION_TITLE = Mode.IDENT_RE + "\\s*\\(";
   private static String KEYWORDS = "int float while private char catch import module export virtual operator sizeof " +
         "dynamic_cast|10 typedef const_cast|10 const for static_cast|10 union namespace " +
         "unsigned long volatile static protected bool template mutable if public friend " +
         "do goto auto void enum else break extern using asm case typeid " +
         "short reinterpret_cast|10 default double register explicit signed typename try this " +
         "switch continue inline delete alignof constexpr decltype " +
         "noexcept static_assert thread_local restrict _Bool complex _Complex _Imaginary " +
         "atomic_bool atomic_char atomic_schar " +
         "atomic_uchar atomic_short atomic_ushort atomic_int atomic_uint atomic_long atomic_ulong atomic_llong " +
         "atomic_ullong new throw return " +
         "and or not";
   private static String KEYWORDS_BUILTIN = "std string cin cout cerr clog stdin stdout stderr stringstream istringstream ostringstream " +
         "auto_ptr deque list queue stack vector map set bitset multiset multimap unordered_set " +
         "unordered_map unordered_multiset unordered_multimap array shared_ptr abort abs acos " +
         "asin atan2 atan calloc ceil cosh cos exit exp fabs floor fmod fprintf fputs free frexp " +
         "fscanf isalnum isalpha iscntrl isdigit isgraph islower isprint ispunct isspace isupper " +
         "isxdigit tolower toupper labs ldexp log10 log malloc realloc memchr memcmp memcpy memset modf pow " +
         "printf putchar puts scanf sinh sin snprintf sprintf sqrt sscanf strcat strchr strcmp " +
         "strcpy strcspn strlen strncat strncmp strncpy strpbrk strrchr strspn strstr tanh tan " +
         "vfprintf vprintf vsprintf endl initializer_list unique_ptr";
   private static String KEYWORDS_LITERAL = "true false nullptr NULL";

   @Override
   public Language build() {
      final Mode CPP_PRIMITIVE_TYPES = new Mode().className("keyword").begin("\\b[a-z\\d_]*_t\\b");
      final Mode STRINGS = new Mode()
            .className("string")
            .variants(new Mode[] {
                  new Mode().begin("(u8?|U)?L?\"").end("\"").illegal("\\n").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                  new Mode().begin("(u8?|U)?R\"").end("\"").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                  new Mode().begin("'\\\\?.").end("'").illegal(".")
            });
      final Mode NUMBERS = new Mode()
            .className("number")
            .relevance(0)
            .variants(new Mode[] {
                  new Mode().begin("\\b(0b[01']+)"),
                  new Mode().begin("(-?)\\b([\\d']+(\\.[\\d']*)?|\\.[\\d']+)(u|U|l|L|ul|UL|f|F|b|B)"),
                  new Mode().begin("(-?)(\\b0[xX][a-fA-F0-9']+|(\\b[\\d']+(\\.[\\d']*)?|\\.[\\d']+)([eE][-+]?[\\d']+)?)")
            });
      final Mode PREPROCESSOR = new Mode()
            .className("meta")
            .begin("#\\s*[a-z]+\\b")
            .end("$")
            .keywords(new Keyword[] { new Keyword("meta-keyword", "if else elif endif define undef warning error line pragma ifdef ifndef include") })
            .contains(new Mode[] {
                  new Mode().begin("\\\\\\n").relevance(0),
                  Mode.inherit(STRINGS, new Mode().className("meta-string")),
                  new Mode().className("meta-string").begin("<[^\\n>]*>").end("$").illegal("\\n"),
                  Mode.C_LINE_COMMENT_MODE,
                  Mode.C_BLOCK_COMMENT_MODE
            });
      final Mode[] EXPRESSION_CONTAINS = new Mode[] {
            CPP_PRIMITIVE_TYPES,
            Mode.C_LINE_COMMENT_MODE,
            Mode.C_BLOCK_COMMENT_MODE,
            NUMBERS,
            STRINGS
      };

      final Keyword[] CPP_KEYWORDS = new Keyword[] {
            new Keyword("keyword", KEYWORDS),
            new Keyword("built_in", KEYWORDS_BUILTIN),
            new Keyword("literal", KEYWORDS_LITERAL)
      };
      
      return (Language) new Language()
            .aliases(ALIASES)
            .keywords(CPP_KEYWORDS)
            .illegal("</")
            .contains(Mode.mergeModes(EXPRESSION_CONTAINS, new Mode[] {
                  PREPROCESSOR,
                  new Mode()
                        .begin("\\b(deque|list|queue|stack|vector|map|set|bitset|multiset|multimap|unordered_map|unordered_set|unordered_multiset|unordered_multimap|array)\\s*<")
                        .end(">")
                        .keywords(CPP_KEYWORDS)
                        .contains(new Mode[] { Mode.SELF, CPP_PRIMITIVE_TYPES }),
                  new Mode().begin(Mode.IDENT_RE + "::").keywords(CPP_KEYWORDS),
                  new Mode()
                        .keywords(CPP_KEYWORDS)
                        .relevance(0)
                        .variants(new Mode[] {
                              new Mode().begin("=").end(";"),
                              new Mode().begin("\\(").end("\\)"),
                              new Mode().beginKeywords(new Keyword[] { new Keyword("keyword", "new throw return else")}).end(";")
                        })
                        .contains(Mode.mergeModes(EXPRESSION_CONTAINS, new Mode[] {
                              new Mode()
                                    .begin("\\(")
                                    .end("\\)")
                                    .keywords(CPP_KEYWORDS)
                                    .relevance(0)
                                    .contains(Mode.mergeModes(EXPRESSION_CONTAINS, new Mode[] { Mode.SELF }))
                        })),
                  new Mode()
                        .className("function")
                        .begin("(" + Mode.IDENT_RE + "[\\*&\\s]+)+" + FUNCTION_TITLE)
                        .end("[{;=]")
                        .returnBegin()
                        .excludeEnd()
                        .keywords(CPP_KEYWORDS)
                        .illegal("[^\\w\\s\\*&]")
                        .contains(new Mode[] {
                              new Mode().begin(FUNCTION_TITLE).returnBegin().contains(new Mode[] { Mode.TITLE_MODE }).relevance(0),
                              new Mode()
                                    .className("params")
                                    .begin("\\(")
                                    .end("\\)")
                                    .keywords(CPP_KEYWORDS)
                                    .relevance(0)
                                    .contains(new Mode[] {
                                          Mode.C_LINE_COMMENT_MODE,
                                          Mode.C_BLOCK_COMMENT_MODE,
                                          STRINGS,
                                          NUMBERS,
                                          CPP_PRIMITIVE_TYPES
                              }),
                              Mode.C_LINE_COMMENT_MODE,
                              Mode.C_BLOCK_COMMENT_MODE,
                              PREPROCESSOR
                        }),
                  new Mode()
                        .className("class")
                        .beginKeywords(new Keyword[] { new Keyword("keyword", "class struct")})
                        .end("[{;:]")
                        .contains(new Mode[] {
                              new Mode().begin("<").end(">").contains(new Mode[] { Mode.SELF }),
                              Mode.TITLE_MODE
                        })
            }));
   }
}
