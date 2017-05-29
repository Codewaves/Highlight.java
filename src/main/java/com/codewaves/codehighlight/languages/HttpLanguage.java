package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/18/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class HttpLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "https" };
   private static final String VERSION = "HTTP\\/[0-9\\.]+";
   @Override
   public Language build() {
      return (Language) new Language()
            .aliases(ALIASES)
            .illegal("\\S")
            .contains(new Mode[] {
                  new Mode().begin("^" + VERSION).end("$").contains(new Mode[] { new Mode().className("number").begin("\\b\\d{3}\\b") }),
                  new Mode()
                        .begin("^[A-Z]+ (.*?) " + VERSION + "$")
                        .returnBegin()
                        .end("$")
                        .contains(new Mode[] {
                              new Mode().className("string").begin(" ").end(" ").excludeBegin().excludeEnd(),
                              new Mode().begin(VERSION),
                              new Mode().className("keyword").begin("[A-Z]+")
                  }),
                  new Mode()
                        .className("attribute")
                        .begin("^\\w")
                        .end(": ")
                        .excludeEnd()
                        .illegal("\\n|\\s|=")
                        .starts(new Mode().end("$").relevance(0)),
                  new Mode().begin("\\n\\n").starts(new Mode().endsWithParent().subLanguages(new String[] { }))
            });
   }
}
