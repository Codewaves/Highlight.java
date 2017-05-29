package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

/**
 * Created by Sergej Kravcenko on 5/21/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class PerlLanguage implements LanguageBuilder {
   private static String[] ALIASES = { "pl", "pm" };
   private static String PERL_KEYWORDS = "getpwent getservent quotemeta msgrcv scalar kill dbmclose undef lc " +
         "ma syswrite tr send umask sysopen shmwrite vec qx utime local oct semctl localtime " +
         "readpipe do return format read sprintf dbmopen pop getpgrp not getpwnam rewinddir qq" +
         "fileno qw endprotoent wait sethostent bless s|0 opendir continue each sleep endgrent " +
         "shutdown dump chomp connect getsockname die socketpair close flock exists index shmget" +
         "sub for endpwent redo lstat msgctl setpgrp abs exit select print ref gethostbyaddr " +
         "unshift fcntl syscall goto getnetbyaddr join gmtime symlink semget splice x|0 " +
         "getpeername recv log setsockopt cos last reverse gethostbyname getgrnam study formline " +
         "endhostent times chop length gethostent getnetent pack getprotoent getservbyname rand " +
         "mkdir pos chmod y|0 substr endnetent printf next open msgsnd readdir use unlink " +
         "getsockopt getpriority rindex wantarray hex system getservbyport endservent int chr " +
         "untie rmdir prototype tell listen fork shmread ucfirst setprotoent else sysseek link " +
         "getgrgid shmctl waitpid unpack getnetbyname reset chdir grep split require caller " +
         "lcfirst until warn while values shift telldir getpwuid my getprotobynumber delete and " +
         "sort uc defined srand accept package seekdir getprotobyname semop our rename seek if q|0 " +
         "chroot sysread setpwent no crypt getc chown sqrt write setnetent setpriority foreach " +
         "tie sin msgget map stat getlogin unless elsif truncate exec keys glob tied closedir" +
         "ioctl socket readlink eval xor readline binmode setservent eof ord bind alarm pipe " +
         "atan2 getgrent exp time push setgrent gt lt or ne m|0 break given say state when";

   @Override
   public Language build() {
      final Mode SUBST = new Mode()
            .begin("[$@]\\{")
            .end("\\}")
            .keywords(new Keyword[] { new Keyword("keyword", "PERL_KEYWORDS") });
      final Mode METHOD = new Mode().begin("->\\{").end("\\}");
      final Mode VAR = new Mode().variants(new Mode[] {
            new Mode().begin("\\$\\d"),
            new Mode().begin("[\\$%@](\\^\\w\\b|#\\w+(::\\w+)*|\\{\\w+\\}|\\w+(::\\w*)*)"),
            new Mode().begin("[\\$%@][^\\s\\w\\{]").relevance(0)
      });
      final Mode[] STRING_CONTAINS = new Mode[] { Mode.BACKSLASH_ESCAPE, SUBST, VAR };
      final Mode[] PERL_DEFAULT_CONTAINS = new Mode[] {
            VAR,
            Mode.HASH_COMMENT_MODE,
            Mode.COMMENT("^\\=\\w", "\\=cut", new Mode().endsWithParent()),
            METHOD,
            new Mode()
                  .className("string")
                  .contains(STRING_CONTAINS)
                  .variants(new Mode[] {
                        new Mode().begin("q[qwxr]?\\s*\\(").end("\\)").relevance(5),
                        new Mode().begin("q[qwxr]?\\s*\\[").end("\\]").relevance(5),
                        new Mode().begin("q[qwxr]?\\s*\\{").end("\\}").relevance(5),
                        new Mode().begin("q[qwxr]?\\s*\\|").end("\\|").relevance(5),
                        new Mode().begin("q[qwxr]?\\s*\\<").end("\\>").relevance(5),
                        new Mode().begin("qw\\s+q").end("q").relevance(5),
                        new Mode().begin("\'").end("\'").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                        new Mode().begin("\"").end("\""),
                        new Mode().begin("`").end("`").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }),
                        new Mode().begin("\\{\\w+\\}").relevance(0),
                        new Mode().begin("-?\\w+\\s*\\=\\>").relevance(0)
            }),
            new Mode()
                  .className("number")
                  .begin("(\\b0[0-7_]+)|(\\b0x[0-9a-fA-F_]+)|(\\b[1-9][0-9_]*(\\.[0-9_]+)?)|[0_]\\b")
                  .relevance(0),
            new Mode()
                  .begin("(\\/\\/|" + Mode.RE_STARTERS_RE + "|\\b(split|return|print|reverse|grep)\\b)\\s*")
                  .keywords(new Keyword[] { new Keyword("keyword", "split return print reverse grep") })
                  .relevance(0)
                  .contains(new Mode[] {
                        Mode.HASH_COMMENT_MODE,
                        new Mode().className("regexp").begin("(s|tr|y)/(\\\\.|[^/])*/(\\\\.|[^/])*/[a-z]*").relevance(10),
                        new Mode().className("regexp").begin("(m|qr)?/").end("/[a-z]*").contains(new Mode[] { Mode.BACKSLASH_ESCAPE }).relevance(0)
            }),
            new Mode()
                  .className("function")
                  .beginKeywords(new Keyword[] { new Keyword("keyword", "sub") })
                  .end("(\\s*\\(.*?\\))?[;{]")
                  .excludeEnd()
                  .relevance(5)
                  .contains(new Mode[] { Mode.TITLE_MODE }),
            new Mode().begin("-\\w\\b").relevance(0),
            new Mode()
                  .begin("^__DATA__$")
                  .end("^__END__$")
                  .subLanguage("mojolicious")
                  .contains(new Mode[] { new Mode().begin("^@@.*").end("$").className("comment") })
      };

      SUBST.contains(PERL_DEFAULT_CONTAINS);
      METHOD.contains(PERL_DEFAULT_CONTAINS);

      return (Language) new Language()
            .aliases(ALIASES)
            .lexemes("[\\w\\.]+")
            .keywords(new Keyword[] { new Keyword("keyword", PERL_KEYWORDS) })
            .contains(PERL_DEFAULT_CONTAINS);
   }
}
