package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/18/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class DiffLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "patch" };
   @Override
   public Language build() {
      return (Language) new Language()
            .aliases(ALIASES)
            .contains(new Mode[] {
                  new Mode().className("meta").relevance(10).variants(new Mode[] {
                        new Mode().begin("^@@ +\\-\\d+,\\d+ +\\+\\d+,\\d+ +@@$"),
                        new Mode().begin("^\\*\\*\\* +\\d+,\\d+ +\\*\\*\\*\\*$"),
                        new Mode().begin("^\\-\\-\\- +\\d+,\\d+ +\\-\\-\\-\\-$")
                  }),
                  new Mode().className("comment").variants(new Mode[] {
                        new Mode().begin("Index: ").end("$"),
                        new Mode().begin("={3,}").end("$"),
                        new Mode().begin("^\\-{3}").end("$"),
                        new Mode().begin("\\*{3} ").end("$"),
                        new Mode().begin("^\\+{3}").end("$"),
                        new Mode().begin("\\*{5}").end("\\*{5}$")
                  }),
                  new Mode().className("addition").begin("^\\+").end("$"),
                  new Mode().className("deletion").begin("^\\-").end("$"),
                  new Mode().className("addition").begin("^\\!").end("$")
            });
   }
}
