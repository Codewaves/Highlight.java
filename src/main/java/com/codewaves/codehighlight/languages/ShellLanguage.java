package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

public class ShellLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "console" };

   public Language build() {
      return (Language) new Language()
            .aliases(ALIASES)
            .contains(new Mode[] {
                  new Mode()
                        .className("meta")
                        .begin("^\\s{0,3}[\\w\\d\\[\\]()@-]*[>%$#]")
                        .starts(new Mode().end("$").subLanguage("bash"))
                  });
   }
}
