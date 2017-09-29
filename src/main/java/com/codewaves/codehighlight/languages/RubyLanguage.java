package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

public class RubyLanguage implements LanguageBuilder {
    @Override
    public Language build() {
        final String RUBY_METHOD_RE = "[a-zA-Z_]\\w*[!?=]?|[-+~]\\@|<<|>>|=~|===?|<=>|[<>]=?|\\*\\*|[-/+%^&*~`|]|\\[\\]=?";

        final String KEYWORDS = "and then defined module in return " +
                "redo if BEGIN retry end for self when next until do begin unless " +
                "END rescue else break undef not super class case require yield alias " +
                "while ensure elsif or include attr_reader attr_writer attr_accessor";
        final String LITERALS = "true false nil";

        final Keyword[] RUBY_KEYWORDS = new Keyword[] {
                new Keyword("keyword", KEYWORDS),
                new Keyword("literal", LITERALS)
        };
        final Mode YARDOCTAG = new Mode().className("doctag").begin("@[A-Za-z]+");
        final Mode IRB_OBJECT = new Mode().begin("#<").end(">");
        final Mode[] COMMENT_MODES = new Mode[] {
                Mode.COMMENT("#", "$", new Mode().contains(new Mode[] { YARDOCTAG })),
                Mode.COMMENT("^\\=begin", "^\\=end", new Mode().contains(new Mode[] {
                        YARDOCTAG
                }).relevance(10)),
                Mode.COMMENT("^__END_", "\\n$", null)
        };
        final Mode SUBST = new Mode().className("subst").begin("#\\{").end("\\}").keywords(RUBY_KEYWORDS);
        final Mode STRING = new Mode().className("string").contains(new Mode[] {
                Mode.BACKSLASH_ESCAPE,
                SUBST
        }).variants(new Mode[] {
                new Mode().begin("'").end("'"),
                new Mode().begin("\"").end("\""),
                new Mode().begin("`").end("`"),
                new Mode().begin("%[qQwWx]?\\(").end("\\)"),
                new Mode().begin("%[qQwWx]?\\[").end("\\]"),
                new Mode().begin("%[qQwWx]?\\{").end("\\}"),
                new Mode().begin("%[qQwWx]?<").end(">"),
                new Mode().begin("%[qQwWx]?/").end("/"),
                new Mode().begin("%[qQwWx]?%").end("%"),
                new Mode().begin("%[qQwWx]?-").end("-"),
                new Mode().begin("%[qQwWx]?\\|").end("\\|"),
                // \B in the beginning suppresses recognition of ?-sequences where ?
                // is the last character of a preceding identifier, as in: `func?4`
                new Mode().begin("\\B\\?(\\\\\\d{1,3}|\\\\x[A-Fa-f0-9]{1,2}|\\\\u[A-Fa-f0-9]{4}|\\\\?\\S)\\b"),
                new Mode().begin("<<(-?)\\w+$").end("^\\s*\\w+$")
        });
        final Mode PARAMS = new Mode().className("params").begin("\\(").end("\\)").endsParent().keywords(RUBY_KEYWORDS);

        final Mode[] RUBY_DEFAULT_CONTAINS = Mode.mergeModes(new Mode[] {
                STRING,
                IRB_OBJECT,
                new Mode().className("class").beginKeywords(new Keyword[] {
                        new Keyword("keyword", "class module")
                }).end("$|;").illegal("=").contains(Mode.mergeModes(new Mode[] {
                        Mode.inherit(Mode.TITLE_MODE, new Mode().begin("[A-Za-z_]\\w*(::\\w+)*(\\?|\\!)?")),
                        new Mode().begin("<\\s*").contains(new Mode[] {
                                new Mode().begin("(" + Mode.IDENT_RE + "::)?" + Mode.IDENT_RE)
                        })
                }, COMMENT_MODES)),
                new Mode().className("function").beginKeywords(new Keyword[] {
                        new Keyword("keyword", "def")
                }).end("$|;").contains(Mode.mergeModes(new Mode[] {
                        Mode.inherit(Mode.TITLE_MODE, new Mode().begin(RUBY_METHOD_RE)),
                        PARAMS
                }, COMMENT_MODES)),
                // swallow namespace qualifiers before symbols
                new Mode().begin(Mode.IDENT_RE + "::"),
                new Mode().className("symbol").begin(Mode.UNDERSCORE_IDENT_RE + "(\\!|\\?)?:").relevance(0),
                new Mode().className("symbol").begin(":(?!\\s)").contains(new Mode[] {
                        STRING,
                        new Mode().begin(RUBY_METHOD_RE)
                }).relevance(0),
                new Mode().className("number")
                        .begin("(\\b0[0-7_]+)|(\\b0x[0-9a-fA-F_]+)|(\\b[1-9][0-9_]*(\\.[0-9_]+)?)|[0_]\\b")
                        .relevance(0),
                // variables
                new Mode().begin("(\\$\\W)|((\\$|\\@\\@?)(\\w+))"),
                new Mode().className("params").begin("\\|").end("\\|").keywords(RUBY_KEYWORDS),
                // regexp container
                new Mode().begin("(" + Mode.RE_STARTERS_RE + "|unless)\\s*").keywords(new Keyword[] {
                        new Keyword("keyword", "unless")
                }).contains(Mode.mergeModes(new Mode[] {
                        IRB_OBJECT,
                        new Mode().className("regexp").contains(new Mode[] {
                                Mode.BACKSLASH_ESCAPE,
                                SUBST
                        }).illegal("\\n").variants(new Mode[] {
                                new Mode().begin("/").end("/[a-z]*"),
                                new Mode().begin("%r\\{").end("\\}[a-z]*"),
                                new Mode().begin("%r\\(").end("\\)[a-z]*"),
                                new Mode().begin("%r!").end("![a-z]*"),
                                new Mode().begin("%r\\[").end("\\][a-z]*")
                        })
                }, COMMENT_MODES)).relevance(0)
        }, COMMENT_MODES);

        SUBST.contains(RUBY_DEFAULT_CONTAINS);
        PARAMS.contains(RUBY_DEFAULT_CONTAINS);

        final String  SIMPLE_PROMPT = "[>?]>";
        final String  DEFAULT_PROMPT = "[\\w#]+\\(\\w+\\):\\d+:\\d+>";
        final String  RVM_PROMPT = "(\\w+-)?\\d+\\.\\d+\\.\\d(p\\d+)?[^>]+>";

        final Mode[] IRB_DEFAULT = new Mode[] {
                new Mode().begin("^\\s*=>").starts(new Mode().end("$").contains(RUBY_DEFAULT_CONTAINS)),
                new Mode().className("meta")
                        .begin("^(" + SIMPLE_PROMPT + "|" + DEFAULT_PROMPT + "|" + RVM_PROMPT + ")")
                        .starts(new Mode().end("$").contains(RUBY_DEFAULT_CONTAINS))
        };

        return (Language) new Language()
                .aliases(new String[] { "rb", "gemspec", "podspec", "thor", "irb" })
                .keywords(RUBY_KEYWORDS)
                .illegal("\\/\\*")
                .contains(Mode.mergeModes(Mode.mergeModes(COMMENT_MODES, IRB_DEFAULT), RUBY_DEFAULT_CONTAINS));
   }
}
