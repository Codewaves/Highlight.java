package com.codewaves.codehighlight.core;

import com.codewaves.codehighlight.languages.ApacheLanguage;
import com.codewaves.codehighlight.languages.BashLanguage;
import com.codewaves.codehighlight.languages.CppLanguage;
import com.codewaves.codehighlight.languages.CsLanguage;
import com.codewaves.codehighlight.languages.CssLanguage;
import com.codewaves.codehighlight.languages.DiffLanguage;
import com.codewaves.codehighlight.languages.HttpLanguage;
import com.codewaves.codehighlight.languages.IniLanguage;
import com.codewaves.codehighlight.languages.JavaLanguage;
import com.codewaves.codehighlight.languages.JsLanguage;
import com.codewaves.codehighlight.languages.JsonLanguage;
import com.codewaves.codehighlight.languages.MakefileLanguage;
import com.codewaves.codehighlight.languages.ObjCLanguage;
import com.codewaves.codehighlight.languages.PerlLanguage;
import com.codewaves.codehighlight.languages.PhpLanguage;
import com.codewaves.codehighlight.languages.PythonLanguage;
import com.codewaves.codehighlight.languages.XmlLanguage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sergej Kravcenko on 5/17/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class Highlighter {
   private static final Map<String, Language> mLanguageMap;
   private static final String[] mLanguages;
   static {
      final Map<String, Language> languages = new HashMap<>();
      registerLanguage("apache", languages, new ApacheLanguage().build());
      registerLanguage("bash", languages, new BashLanguage().build());
      registerLanguage("cpp", languages, new CppLanguage().build());
      registerLanguage("cs", languages, new CsLanguage().build());
      registerLanguage("css", languages, new CssLanguage().build());
      registerLanguage("diff", languages, new DiffLanguage().build());
      registerLanguage("http", languages, new HttpLanguage().build());
      registerLanguage("ini", languages, new IniLanguage().build());
      registerLanguage("java", languages, new JavaLanguage().build());
      registerLanguage("javascript", languages, new JsLanguage().build());
      registerLanguage("json", languages, new JsonLanguage().build());
      registerLanguage("makefile", languages, new MakefileLanguage().build());
      registerLanguage("xml", languages, new XmlLanguage().build());
      registerLanguage("objectivec", languages, new ObjCLanguage().build());
      registerLanguage("perl", languages, new PerlLanguage().build());
      registerLanguage("php", languages, new PhpLanguage().build());
      registerLanguage("python", languages, new PythonLanguage().build());
      mLanguageMap = languages;
      mLanguages = new String[] { "apache", "bash", "cpp", "cs", "css", "diff", "http", "ini", "java",
            "javascript", "json", "makefile", "xml", "objectivec", "perl", "php", "python" };
   }

   private static void registerLanguage(String name, Map<String, Language> languages, Language language) {
      languages.put(name, language);
      if (language.aliases != null) {
         for (String alias : language.aliases) {
            languages.put(alias, language);
         }
      }
   }

   static Language findLanguage(String name) {
      return mLanguageMap.get(name);
   }

   public static class HighlightResult {
      private int relevance;
      private String language;
      private CharSequence result;

      HighlightResult(int relevance, String language, CharSequence result) {
         this.relevance = relevance;
         this.language = language;
         this.result = result;
      }

      public int getRelevance() {
         return relevance;
      }

      public String getLanguage() {
         return language;
      }

      public CharSequence getResult() {
         return result;
      }
   }

   private StyleRendererFactory mRendererFactory;

   public Highlighter(StyleRendererFactory factory) {
      mRendererFactory = factory;
   }

   public HighlightResult highlight(String languageName, String code) {
      // Find language by name
      final Language language = mLanguageMap.get(languageName);
      if (language == null) {
         return new HighlightResult(0, null, code);
      }

      // Parse
      final StyleRenderer renderer = mRendererFactory.create();
      final HighlightParser parser = new HighlightParser(language, mRendererFactory, renderer);
      final int relevance = parser.highlight(code, false, null);
      return new HighlightResult(relevance, languageName, renderer.getResult());
   }

   public HighlightResult highlightAuto(String code, String[] languageSubset) {
      final String[] languages = (languageSubset == null || languageSubset.length == 0) ? mLanguages : languageSubset;

      int bestRelevance = 0;
      String bestLanguageName = null;
      CharSequence result = null;
      for (String languageName : languages) {
         final Language language = mLanguageMap.get(languageName);
         if (language == null) {
            continue;
         }

         final StyleRenderer renderer = mRendererFactory.create();
         final HighlightParser parser = new HighlightParser(language, mRendererFactory, renderer);
         final int relevance = parser.highlight(code, false, null);
         if (relevance > bestRelevance) {
            bestRelevance = relevance;
            bestLanguageName = languageName;
            result = renderer.getResult();
         }
      }

      return new HighlightResult(bestRelevance, bestLanguageName, result);
   }
}
