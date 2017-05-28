package com.codewaves.codehighlight.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by Sergej Kravcenko on 5/12/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class Language extends Mode {
   private static final Pattern emptyLexemePattern = Pattern.compile("\\w+", Pattern.MULTILINE);
   private static final Pattern emptyBeginEndPattern = Pattern.compile("\\B|\\b", Pattern.MULTILINE);

   String[] aliases;
   boolean caseInsensitive;

   // Builder
   public Language() {
      super();
   }

   public Language aliases(String[] value) {
      aliases = value;
      return this;
   }

   public Mode caseInsensitive() {
      caseInsensitive = true;
      return this;
   }

   // Compilation
   private static String strJoin(String[] arr, String sep) {
      StringBuilder str = new StringBuilder();
      for (int i = 0, l = arr.length; i < l; i++) {
         if (i > 0)
            str.append(sep);
         str.append(arr[i]);
      }
      return str.toString();
   }

   private String langRe(String re) {
      return "(?m" + (caseInsensitive ? "i" : "") + ")" + re;
   }

   private Mode[] expandMode(Mode mode) {
      final ArrayList<Mode> variants = new ArrayList<>();
      if (mode.variants != null) {
         for (Mode variant :  mode.variants) {
            variants.add(Mode.inherit(mode, variant));
         }
         return variants.toArray(new Mode[variants.size()]);
      }

      if (mode.endsWithParent) {
         return new Mode[] { inherit(mode, null) };
      }
      return new Mode[] { mode };
   }

   private void compileMode(Mode mode, Mode parent) {
      if (mode.compiled) {
         return;
      }
      mode.compiled = true;

      // Keywords
      mode.keywords = mode.keywords == null ? mode.beginKeywords : mode.keywords;
      if (mode.keywords != null) {
         final HashMap<String, Keyword> compiledKeywords = new HashMap<>();
         for (Keyword keywordGroup : mode.keywords) {
            final String keywords = caseInsensitive ? keywordGroup.value.toLowerCase() : keywordGroup.value;
            for (String keyword : keywords.split(" ")) {
               final String[] pair = keyword.split("\\|");
               compiledKeywords.put(pair[0], new Keyword(pair[0], keywordGroup.className, pair.length > 1 ? Integer.valueOf(pair[1]) : 1));
            }
         }
         mode.compiledKeywords = compiledKeywords;
      }

      // Lexemes
      mode.lexemesRe = mode.lexemes == null ? emptyLexemePattern : Pattern.compile(langRe(mode.lexemes));

      // Parent
      if (parent != null) {
         if (mode.beginKeywords != null) {
            mode.begin = "\\b(" + strJoin(mode.beginKeywords[0].value.split(" "), "|") + ")\\b";
         }
         if (mode.begin == null) {
            mode.begin = "\\B|\\b";
            mode.beginRe = emptyBeginEndPattern;
         }
         else {
            mode.beginRe = Pattern.compile(langRe(mode.begin));
         }

         if (mode.end == null && !mode.endsWithParent) {
            mode.end = "\\B|\\b";
            mode.endRe = emptyBeginEndPattern;
         }
         else if (mode.end != null && !mode.end.isEmpty()) {
            mode.endRe = Pattern.compile(langRe(mode.end));
         }

         mode.terminatorEnd = mode.end == null ? "" : mode.end;
         if (mode.endsWithParent && parent.terminatorEnd != null) {
            mode.terminatorEnd += (mode.end == null ? "" : "|") + parent.terminatorEnd;
         }
      }

      // Relevance
      if (mode.relevance == -1) {
         mode.relevance = 1;
      }

      // Illegal
      if (mode.illegal != null) {
         mode.illegalRe = Pattern.compile(langRe(mode.illegal));
      }

      // Contains
      if (mode.contains == null) {
         mode.contains = new Mode[] {};
      }
      final ArrayList<Mode> expandedContains = new ArrayList<>();
      for (Mode childMode : mode.contains) {
         final Mode[] chunk = expandMode(childMode.self ? mode : childMode);
         expandedContains.addAll(Arrays.asList(chunk));
      }
      mode.contains = expandedContains.toArray(new Mode[expandedContains.size()]);
      for (Mode childMode : mode.contains) {
         compileMode(childMode, mode);
      }

      // Starts
      if (mode.starts != null) {
         compileMode(mode.starts, parent);
      }

      // Terminators
      final ArrayList<String> terminators = new ArrayList<>();
      for (Mode childMode : mode.contains) {
         final String term = childMode.beginKeywords != null ? "\\.?(" + childMode.begin + ")\\.?" : childMode.begin;
         if (term != null && term.length() > 0) {
            terminators.add(term);
         }
      }
      if (mode.terminatorEnd != null && mode.terminatorEnd.length() > 0) {
         terminators.add(mode.terminatorEnd);
      }
      if (mode.illegal != null && mode.illegal.length() > 0) {
         terminators.add(mode.illegal);
      }
      mode.terminators = terminators.size() > 0 ? Pattern.compile(langRe(strJoin(terminators.toArray(new String[terminators.size()]), "|"))) : null;
   }

   void compile() {
      synchronized (Language.class) {
         compileMode(this, null);
      }
   }
}
