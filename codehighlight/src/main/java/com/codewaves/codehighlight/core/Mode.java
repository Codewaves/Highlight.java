package com.codewaves.codehighlight.core;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by Sergej Kravcenko on 5/12/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class Mode {
   boolean self;
   boolean compiled;
   HashMap<String, Keyword> compiledKeywords;
   Pattern beginRe;
   Pattern endRe;
   Pattern lexemesRe;
   Pattern illegalRe;

   String subLanguage;
   String[] subLanguages;
   Mode starts;
   Mode[] contains;
   Mode[] variants;
   Keyword[] beginKeywords;
   Keyword[] keywords;
   String className;
   String begin;
   String end;
   String lexemes;
   Pattern terminators;
   String illegal;
   String terminatorEnd;
   int relevance = -1;
   boolean skip;
   boolean returnBegin;
   boolean excludeBegin;
   boolean returnEnd;
   boolean excludeEnd;
   boolean endsWithParent;
   boolean endsParent;

   public static final Mode SELF = new Mode().self();
   public static final String IDENT_RE = "[a-zA-Z]\\w*";
   public static final String UNDERSCORE_IDENT_RE = "[a-zA-Z_]\\w*";
   public static final String NUMBER_RE = "\\b\\d+(\\.\\d+)?";
   public static final String C_NUMBER_RE = "(-?)(\\b0[xX][a-fA-F0-9]+|(\\b\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?)"; // 0x..., 0..., decimal, float
   public static final String BINARY_NUMBER_RE = "\\b(0b[01]+)"; // 0b...
   public static final String RE_STARTERS_RE = "!|!=|!==|%|%=|&|&&|&=|\\*|\\*=|\\+|\\+=|,|-|-=|/=|/|:|;|<<|<<=|<=|<|===|==|=|>>>=|>>=|>=|>>>|>>|>|\\?|\\[|\\{|\\(|\\^|\\^=|\\||\\|=|\\|\\||~";

   public static final Mode BACKSLASH_ESCAPE = new Mode().begin("\\\\[\\s\\S]").relevance(0);
   public static final Mode APOS_STRING_MODE = new Mode().className("string").begin("\'").end("\'").illegal("\\n").contains(new Mode[]{ BACKSLASH_ESCAPE });
   public static final Mode QUOTE_STRING_MODE = new Mode().className("string").begin("\"").end("\"").illegal("\\n").contains(new Mode[]{ BACKSLASH_ESCAPE });
   public static final Mode PHRASAL_WORDS_MODE = new Mode().begin("\\b(a|an|the|are|I'm|isn't|don't|doesn't|won't|but|just|should|pretty|simply|enough|gonna|going|wtf|so|such|will|you|your|they|like|more)\\b");
   public static Mode COMMENT(String begin, String end, Mode inherits) {
      Mode mode = new Mode().className("comment").begin(begin).end(end).contains(new Mode[] {});
      if (inherits != null) {
         mode = inherit(mode, inherits);
      }
      mode.contains = mergeModes(mode.contains, new Mode[] { PHRASAL_WORDS_MODE, new Mode().className("doctag").begin("(?:TODO|FIXME|NOTE|BUG|XXX):").relevance(0) });
      return mode;
   }
   public static final Mode C_LINE_COMMENT_MODE = COMMENT("//", "$", null);
   public static final Mode C_BLOCK_COMMENT_MODE = COMMENT("/\\*", "\\*/", null);
   public static final Mode HASH_COMMENT_MODE = COMMENT("#", "$", null);
   public static final Mode NUMBER_MODE = new Mode().className("number").begin(NUMBER_RE).relevance(0);
   public static final Mode C_NUMBER_MODE = new Mode().className("number").begin(C_NUMBER_RE).relevance(0);
   public static final Mode BINARY_NUMBER_MODE = new Mode().className("number").begin(BINARY_NUMBER_RE).relevance(0);
   public static final Mode TITLE_MODE = new Mode().className("title").begin(IDENT_RE).relevance(0);
   public static final Mode UNDERSCORE_TITLE_MODE = new Mode().className("title").begin(UNDERSCORE_IDENT_RE).relevance(0);
   public static final Mode METHOD_GUARD = new Mode().begin("\\.\\s*" + UNDERSCORE_IDENT_RE).relevance(0);
   public static final Mode CSS_NUMBER_MODE = new Mode().className("number").begin(Mode.NUMBER_RE + "(" +
         "%|em|ex|ch|rem"  +
         "|vw|vh|vmin|vmax" +
         "|cm|mm|in|pt|pc|px" +
         "|deg|grad|rad|turn" +
         "|s|ms" +
         "|Hz|kHz" +
         "|dpi|dpcm|dppx" +
         ")?").relevance(0);
   public static final Mode REGEXP_MODE = new Mode().className("regexp").begin("\\/").end("\\/[gimuy]*").illegal("\\n").contains(new Mode[] {
         BACKSLASH_ESCAPE,
         new Mode().begin("\\[").end("\\]").relevance(0).contains(new Mode[] { BACKSLASH_ESCAPE })
   });

   public static Mode[] mergeModes(Mode[] a, Mode[] b) {
      Mode[] merge = new Mode[a.length + b.length];
      System.arraycopy(a, 0, merge, 0, a.length);
      System.arraycopy(b, 0, merge, a.length, b.length);
      return merge;
   }

   // Copy
   public Mode(Mode other) {
      this.compiled = other.compiled;
      this.compiledKeywords = other.compiledKeywords;

      this.subLanguage = other.subLanguage;
      this.subLanguages = other.subLanguages;
      this.starts = other.starts;
      this.contains = other.contains;
      this.variants = other.variants;
      this.beginKeywords = other.beginKeywords;
      this.keywords = other.keywords;
      this.className = other.className;
      this.begin = other.begin;
      this.end = other.end;
      this.lexemes = other.lexemes;
      this.terminators = other.terminators;
      this.illegal = other.illegal;
      this.terminatorEnd = other.terminatorEnd;
      this.relevance = other.relevance;
      this.skip = other.skip;
      this.returnBegin = other.returnBegin;
      this.excludeBegin = other.excludeBegin;
      this.returnEnd = other.returnEnd;
      this.excludeEnd = other.excludeEnd;
      this.endsWithParent = other.endsWithParent;
      this.endsParent = other.endsParent;
   }

   public static Mode inherit(Mode mode, Mode obj) {
      final Mode other = new Mode(mode);

      if (obj != null) {
         if (obj.starts != null) other.starts = obj.starts;
         if (obj.contains != null) other.contains = obj.contains;
         if (obj.variants != null) other.variants = obj.variants;
         if (obj.className != null) other.className = obj.className;
         if (obj.keywords != null) other.keywords = obj.keywords;
         if (obj.beginKeywords != null) other.beginKeywords = obj.beginKeywords;
         if (obj.begin != null) other.begin = obj.begin;
         if (obj.end != null) other.end = obj.end;
         if (obj.illegal != null) other.illegal = obj.illegal;
         if (obj.relevance != -1) other.relevance = obj.relevance;
         if (obj.returnBegin) other.returnBegin = true;
         if (obj.excludeBegin) other.excludeBegin = true;
         if (obj.returnEnd) other.returnEnd = true;
         if (obj.excludeEnd) other.excludeEnd = true;
         if (obj.endsWithParent) other.endsWithParent = true;
         if (obj.endsParent) other.endsParent = true;
      }
      
      return other;
   }

   // Builder
   public Mode() {
   }

   public Mode[] getContains() {
      return contains;
   }

   public Mode subLanguages(String[] languages) {
      subLanguages = languages;
      return this;
   }

   public Mode subLanguage(String language) {
      subLanguage = language;
      return this;
   }

   public Mode self() {
      self = true;
      return this;
   }

   public Mode className(String value) {
      className = value;
      return this;
   }

   public Mode starts(Mode value) {
      starts = value;
      return this;
   }

   public Mode contains(Mode[] value) {
      contains = value;
      return this;
   }

   public Mode variants(Mode[] value) {
      variants = value;
      return this;
   }

   public Mode beginKeywords(Keyword[] value) {
      beginKeywords = value;
      return this;
   }

   public Mode keywords(Keyword[] value) {
      keywords = value;
      return this;
   }

   public Mode begin(String value) {
      begin = value;
      return this;
   }

   public Mode end(String value) {
      end = value;
      return this;
   }

   public Mode illegal(String value) {
      illegal = value;
      return this;
   }

   public Mode lexemes(String value) {
      lexemes = value;
      return this;
   }

   public Mode relevance(int value) {
      relevance = value;
      return this;
   }

   public Mode skip() {
      skip = true;
      return this;
   }

   public Mode returnBegin() {
      returnBegin = true;
      return this;
   }

   public Mode excludeBegin() {
      excludeBegin = true;
      return this;
   }

   public Mode returnEnd() {
      returnEnd = true;
      return this;
   }

   public Mode excludeEnd() {
      excludeEnd = true;
      return this;
   }

   public Mode endsWithParent() {
      endsWithParent = true;
      return this;
   }

   public Mode endsParent() {
      endsParent = true;
      return this;
   }
}
