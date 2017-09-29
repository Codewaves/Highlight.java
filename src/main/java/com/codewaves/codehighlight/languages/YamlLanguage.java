package com.codewaves.codehighlight.languages;

import com.codewaves.codehighlight.core.Keyword;
import com.codewaves.codehighlight.core.Language;
import com.codewaves.codehighlight.core.Mode;

public class YamlLanguage implements LanguageBuilder {

    private static String keyPrefix = "^[ \\-]*";
    private static String keyName = "[a-zA-Z_][\\w\\-]*";
    private static String hashKey = keyPrefix + keyName + ":";

    @Override
    public Language build() {
        final String LITERALS = "true false yes no null";
        final Mode KEY = new Mode().className("attr").variants(new Mode[] {
                new Mode().begin(hashKey),
                new Mode().begin(keyPrefix + "\"" + keyName + "\"" + ":"),
                new Mode().begin(keyPrefix + "'" + keyName + "'" + ":")
        });

        final Mode TEMPLATE_VARIABLES = new Mode().className("template-variable").variants(new Mode[] {
                // jinja templates Ansible
                new Mode().begin("\\{\\{").end("\\}\\}"),
                // Ruby i18n
                new Mode().begin("%\\{").end("\\}")
        });
        final Mode[] stringContains = new Mode[] { Mode.BACKSLASH_ESCAPE, TEMPLATE_VARIABLES };
        final Mode STRING = new Mode().className("string").relevance(0).variants(new Mode[] {
                new Mode().begin("'").end("'"),
                new Mode().begin("\"").end("\""),
                new Mode().begin("\\S+")
            }).contains(stringContains);

        return (Language) new Language()
                .aliases(new String[] { "yml", "YAML", "yaml" })
                .caseInsensitive()
                .contains(new Mode[] {
                        KEY,
                        new Mode().className("meta").begin("^---\\s*$").relevance(10),
                        // multi line string
                        new Mode().className("string").begin("[\\|>] *$").returnEnd().contains(stringContains).end(hashKey),
                        // Ruby/Rails erb
                        new Mode().begin("<%[%=-]?").end("[%-]?%>").subLanguage("ruby").excludeBegin().excludeEnd().relevance(0),
                        // data type
                        new Mode().className("type").begin("!!" + Mode.UNDERSCORE_IDENT_RE),
                        // fragment id &ref
                        new Mode().className("meta").begin("&" + Mode.UNDERSCORE_IDENT_RE + "$"),
                        // fragment reference *ref
                        new Mode().className("meta").begin("\\*" + Mode.UNDERSCORE_IDENT_RE + "$"),
                        // array listing
                        new Mode().className("bullet").begin("^ *-").relevance(0),
                        Mode.HASH_COMMENT_MODE,
                        new Mode().beginKeywords(new Keyword[] {
                                new Keyword("keyword", LITERALS)
                        }).keywords(new Keyword[] {
                                new Keyword("literal", LITERALS)
                        }),
                        Mode.C_NUMBER_MODE,
                        STRING
            });
   }
}
