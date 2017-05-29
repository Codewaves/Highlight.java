package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/20/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class XmlLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "html", "xhtml", "rss", "atom", "xjb", "xsd", "xsl", "plist" };
   private static final String XML_IDENT_RE = "[A-Za-z0-9\\._:-]+";

   @Override
   public Language build() {
      final Mode TAG_INTERNALS = new Mode()
            .endsWithParent()
            .illegal("<")
            .relevance(0)
            .contains(new Mode[] {
                  new Mode().className("attr").begin(XML_IDENT_RE).relevance(0),
                  new Mode()
                        .begin("=\\s*")
                        .relevance(0)
                        .contains(new Mode[] {
                              new Mode().className("string").endsParent().variants(new Mode[] {
                                    new Mode().begin("\"").end("\""),
                                    new Mode().begin("\'").end("\'"),
                                    new Mode().begin("[^\\s\"\'=<>`]+")
                              })
                  })
            });

      return (Language) new Language()
            .aliases(ALIASES)
            .caseInsensitive()
            .contains(new Mode[] {
                  new Mode()
                        .className("meta")
                        .begin("<!DOCTYPE")
                        .end(">")
                        .relevance(10)
                        .contains(new Mode[] { new Mode().begin("\\[").end("\\]") }),
                  Mode.COMMENT("<!--", "-->", new Mode().relevance(10)),
                  new Mode().begin("<\\!\\[CDATA\\[").end("\\]\\]>").relevance(10),
                  new Mode()
                        .begin("<\\?(php)?")
                        .end("\\?")
                        .subLanguage("php")
                        .contains(new Mode[] { new Mode().begin("/\\*").end("\\*/").skip() }),
                  new Mode()
                        .className("tag")
                        .begin("<style(?=\\s|>|$)")
                        .end(">")
                        .keywords(new Keyword[] { new Keyword("name", "style") })
                        .contains(new Mode[] { TAG_INTERNALS })
                        .starts(new Mode().end("\\<\\/style\\>").returnEnd().subLanguages(new String[] { "css", "xml" })),
                  new Mode()
                        .className("tag")
                        .begin("<script(?=\\s|>|$)")
                        .end(">")
                        .keywords(new Keyword[] { new Keyword("name", "script") })
                        .contains(new Mode[] { TAG_INTERNALS })
                        .starts(new Mode().end("\\<\\/script\\>").returnEnd().subLanguages(new String[] { "actionscript", "javascript", "handlebars", "xml" })),
                  new Mode()
                        .className("meta")
                        .variants(new Mode[] {
                        new Mode().begin("<\\?xml").end("\\?>").relevance(10),
                        new Mode().begin("<\\?\\w+").end("\\?>")
                  }),
                  new Mode()
                        .className("tag")
                        .begin("</?")
                        .end("/?>")
                        .contains(new Mode[] {
                              new Mode().className("name").begin("[^\\/><\\s]+").relevance(0),
                              TAG_INTERNALS
                  })
            });
   }
}
