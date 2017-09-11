package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/17/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class BashLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "sh", "zsh" };
   private static String KEYWORDS = "if then else elif fi for while in do done case esac function";
   private static String KEYWORDS_LITERAL = "true false";
   private static String KEYWORDS_BUILTIN = "break cd continue eval exec exit export getopts hash pwd readonly return shift test times " +
         "trap umask unset alias bind builtin caller command declare echo enable help let local logout mapfile printf " +
         "read readarray source type typeset ulimit unalias set shopt " +
         "autoload bg bindkey bye cap chdir clone comparguments compcall compctl compdescribe compfiles " +
         "compgroups compquote comptags comptry compvalues dirs disable disown echotc echoti emulate " +
         "fc fg float functions getcap getln history integer jobs kill limit log noglob popd print " +
         "pushd pushln rehash sched setcap setopt stat suspend ttyctl unfunction unhash unlimit " +
         "unsetopt vared wait whence where which zcompile zformat zftp zle zmodload zparseopts zprof " +
         "zpty zregexparse zsocket zstyle ztcp";
   private static String KEYWORDS_REL = "-ne -eq -lt -gt -f -d -e -s -l -a";

   @Override
   public Language build() {
      final Mode VAR = new Mode()
            .className("variable")
            .variants(new Mode[] {
                  new Mode().begin("\\$[\\w\\d#@][\\w\\d_]*"),
                  new Mode().begin("\\$\\{(.*?)\\}")
            });
      final Mode QUOTE_STRING = new Mode()
            .className("string")
            .begin("\"")
            .end("\"")
            .contains(new Mode[] {
                  Mode.BACKSLASH_ESCAPE,
                  VAR,
                  new Mode().className("variable")
                        .begin("\\$\\(")
                        .end("\\)")
                        .contains(new Mode[] { Mode.BACKSLASH_ESCAPE })
            });
      final Mode APOS_STRING = new Mode()
            .className("string")
            .begin("'")
            .end("'");

      return (Language) new Language()
            .aliases(ALIASES)
            .lexemes("\\b-?[a-z\\._]+\\b")
            .keywords(new Keyword[] {
                  new Keyword("keyword", KEYWORDS),
                  new Keyword("literal", KEYWORDS_LITERAL),
                  new Keyword("built_in", KEYWORDS_BUILTIN),
                  new Keyword("_", KEYWORDS_REL)
            })
            .contains(new Mode[] {
                  new Mode().className("meta").begin("^#![^\\n]+sh\\s*$").relevance(10),
                  new Mode()
                        .className("function")
                        .begin("\\w[\\w\\d_]*\\s*\\(\\s*\\)\\s*\\{")
                        .returnBegin()
                        .contains(new Mode[] { Mode.inherit(Mode.TITLE_MODE, new Mode().begin("\\w[\\w\\d_]*")) })
                        .relevance(0),
                  Mode.HASH_COMMENT_MODE,
                  QUOTE_STRING,
                  APOS_STRING,
                  VAR
            });
   }
}
