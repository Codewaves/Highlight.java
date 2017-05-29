package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/20/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class JsonLanguage implements LanguageBuilder {
   @Override
   public Language build() {
      final Keyword[] LITERALS = new Keyword[] { new Keyword("literal", "true false null") };
      final Mode[] TYPES = new Mode[] { Mode.QUOTE_STRING_MODE, Mode.C_NUMBER_MODE, null, null };
      final Mode VALUE_CONTAINER = new Mode()
            .end(",")
            .endsWithParent()
            .excludeEnd()
            .contains(TYPES)
            .keywords(LITERALS);
      final Mode OBJECT = new Mode()
            .begin("\\{")
            .end("\\}")
            .illegal("\\S")
            .contains(new Mode[] {
                  new Mode()
                        .className("attr")
                        .begin("\"")
                        .end("\"")
                        .illegal("\\n")
                        .contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                  Mode.inherit(VALUE_CONTAINER, new Mode().begin(":"))
            });
      final Mode ARRAY = new Mode()
            .begin("\\[")
            .end("\\]")
            .illegal("\\S")
            .contains(new Mode[] { Mode.inherit(VALUE_CONTAINER, null) });
      TYPES[2] = OBJECT;
      TYPES[3] = ARRAY;

      return (Language) new Language()
            .contains(TYPES)
            .keywords(LITERALS)
            .illegal("\\S");

   }
}
