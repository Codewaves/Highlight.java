package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/21/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class ObjCLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "mm", "objc", "obj-c" };
   private static String KEYWORDS = "int float while char export sizeof typedef const struct for union " +
         "unsigned long volatile static bool mutable if do return goto void " +
         "enum else break extern asm case short default double register explicit " +
         "signed typename this switch continue wchar_t inline readonly assign " +
         "readwrite self @synchronized id typeof " +
         "nonatomic super unichar IBOutlet IBAction strong weak copy " +
         "in out inout bycopy byref oneway __strong __weak __block __autoreleasing " +
         "@private @protected @public @try @property @end @throw @catch @finally " +
         "@autoreleasepool @synthesize @dynamic @selector @optional @required " +
         "@encode @package @import @defs @compatibility_alias " +
         "__bridge __bridge_transfer __bridge_retained __bridge_retain " +
         "__covariant __contravariant __kindof " +
         "_Nonnull _Nullable _Null_unspecified " +
         "__FUNCTION__ __PRETTY_FUNCTION__ __attribute__ " +
         "getter setter retain unsafe_unretained " +
         "nonnull nullable null_unspecified null_resettable class instancetype " +
         "NS_DESIGNATED_INITIALIZER NS_UNAVAILABLE NS_REQUIRES_SUPER " +
         "NS_RETURNS_INNER_POINTER NS_INLINE NS_AVAILABLE NS_DEPRECATED " +
         "NS_ENUM NS_OPTIONS NS_SWIFT_UNAVAILABLE " +
         "NS_ASSUME_NONNULL_BEGIN NS_ASSUME_NONNULL_END " +
         "NS_REFINED_FOR_SWIFT NS_SWIFT_NAME NS_SWIFT_NOTHROW " +
         "NS_DURING NS_HANDLER NS_ENDHANDLER NS_VALUERETURN NS_VOIDRETURN";
   private static String KEYWORDS_BUILTIN = "BOOL dispatch_once_t dispatch_queue_t dispatch_sync dispatch_async dispatch_once";
   private static String KEYWORDS_LITERAL = "false true FALSE TRUE nil YES NO NULL";
   private static String KEYWORDS_CLASS = "@interface|@class|@protocol|@implementation";
   private static String KEYWORDS_CLASS2 = "@interface @class @protocol @implementation";
   private static String LEXEMES = "[a-zA-Z@][a-zA-Z0-9_]*";
   
   @Override
   public Language build() {
      final Keyword[] OBJC_KEYWORDS = new Keyword[] {
            new Keyword("keyword", KEYWORDS),
            new Keyword("built_in", KEYWORDS_BUILTIN),
            new Keyword("literal", KEYWORDS_LITERAL)
      };

      final Mode API_CLASS = new Mode()
            .className("built_in")
            .begin("\\b(AV|CA|CF|CG|CI|CL|CM|CN|CT|MK|MP|MTK|MTL|NS|SCN|SK|UI|WK|XC)\\w+");

      return (Language) new Language()
            .aliases(ALIASES)
            .keywords(OBJC_KEYWORDS)
            .lexemes(LEXEMES)
            .illegal("</")
            .contains(new Mode[] {
                  API_CLASS,
                  Mode.C_LINE_COMMENT_MODE,
                  Mode.C_BLOCK_COMMENT_MODE,
                  Mode.C_NUMBER_MODE,
                  Mode.QUOTE_STRING_MODE,
                  new Mode()
                        .className("string")
                        .variants(new Mode[] {
                              new Mode().begin("@\"").end("\"").illegal("\\n").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                              new Mode().begin("\'").end("[^\\\\]\'").illegal("[^\\\\][^\']")
                  }),
                  new Mode()
                        .className("meta")
                        .begin("#")
                        .end("$")
                        .contains(new Mode[] {
                              new Mode()
                                    .className("meta-string")
                                    .variants(new Mode[] {
                                          new Mode().begin("\"").end("\""),
                                          new Mode().begin("<").end(">")
                              })
                  }),
                  new Mode()
                        .className("class")
                        .begin("(" + KEYWORDS_CLASS + ")\\b")
                        .end("(\\{|$)")
                        .excludeEnd()
                        .keywords(new Keyword[] { new Keyword("keyword", KEYWORDS_CLASS2) })
                        .lexemes(LEXEMES)
                        .contains(new Mode[] { Mode.UNDERSCORE_TITLE_MODE }),
                  new Mode().begin("\\." + Mode.UNDERSCORE_IDENT_RE).relevance(0)
            });
   }
}
