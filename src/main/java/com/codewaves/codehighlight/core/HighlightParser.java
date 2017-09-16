package com.codewaves.codehighlight.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sergej Kravcenko on 5/13/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

class HighlightParser {
   private Language mLanguage;
   private StyleRendererFactory mRendererFactory;
   private StyleRenderer mBlockRenderer;

   private String mModeBuffer;
   private ParentWrapper mTop;
   private HashMap<String, ParentWrapper> mContinuations;
   private boolean mIgnoreIllegals;
   private int mRelevance;

   private static class ParentWrapper {
      private Mode mode;
      private ParentWrapper parent;

      private ParentWrapper(Mode mode, ParentWrapper parent) {
         this.mode = mode;
         this.parent = parent;
      }
   }

   HighlightParser(Language language, StyleRendererFactory rendererFactory, StyleRenderer renderer) {
      mLanguage = language;
      mRendererFactory = rendererFactory;
      mBlockRenderer = renderer;
   }

   private boolean testRe(Pattern re, String lexeme) {
      if (re == null) {
         return false;
      }

      final Matcher matcher = re.matcher(lexeme);
      return matcher.find() && matcher.start() == 0;
   }

   private Mode subMode(String lexeme, Mode mode) {
      for (int i = 0, length = mode.contains.length; i < length; i++) {
         if (testRe(mode.contains[i].beginRe, lexeme)) {
            return mode.contains[i];
         }
      }
      return null;
   }

   private void startNewMode(Mode mode) {
      if (mode.className != null) {
         mBlockRenderer.onPushStyle(mode.className);
      }
      mTop = new ParentWrapper(mode, mTop);
   }

   private ParentWrapper endOfMode(ParentWrapper mode, String lexeme) {
      if (testRe(mode.mode.endRe, lexeme)) {
         while (mode.mode.endsParent && mode.parent != null) {
            mode = mode.parent;
         }
         return mode;
      }
      if (mode.mode.endsWithParent) {
         return endOfMode(mode.parent, lexeme);
      }
      return null;
   }

   private void processBuffer() {
      if (mTop.mode.subLanguage != null || mTop.mode.subLanguages != null) {
         processSubLanguage();
      }
      else {
         processKeywords();
      }
      mModeBuffer = "";
   }

   private boolean isIllegal(String lexeme, Mode mode) {
      return !mIgnoreIllegals && testRe(mode.illegalRe, lexeme);
   }

   private Keyword keywordMatch(Mode mode, String match) {
      final String matchStr = mLanguage.caseInsensitive ? match.toLowerCase() : match;
      if (mode.compiledKeywords != null) {
         return mode.compiledKeywords.get(matchStr);
      }
      return null;
   }

   private void processKeywords() {
      if (mTop.mode.compiledKeywords == null) {
         mBlockRenderer.onPushCodeBlock(mModeBuffer);
         return;
      }

      int lastIndex = 0;

      final Matcher matcher = mTop.mode.lexemesRe.matcher(mModeBuffer);
      while (matcher.find()) {
         mBlockRenderer.onPushCodeBlock(mModeBuffer.substring(lastIndex, matcher.start()));
         final Keyword keyword = keywordMatch(mTop.mode, matcher.group());
         if (keyword != null) {
            mRelevance += keyword.relevance;
            mBlockRenderer.onPushStyle(keyword.className);
            mBlockRenderer.onPushCodeBlock(matcher.group());
            mBlockRenderer.onPopStyle();
         }
         else {
            mBlockRenderer.onPushCodeBlock(matcher.group());
         }
         lastIndex = matcher.end();
      }
      mBlockRenderer.onPushCodeBlock(mModeBuffer.substring(lastIndex));
   }

   private void processSubLanguage() {
      final boolean explicit = mTop.mode.subLanguage != null;
      int relevance;
      CharSequence resultCode;
      String resultLanguage;

      if (explicit) {
         final Language language = Highlighter.findLanguage(mTop.mode.subLanguage);
         if (language == null) {
            mBlockRenderer.onPushSubLanguage(null, mModeBuffer);
            return;
         }

         final StyleRenderer renderer = mRendererFactory.create(mTop.mode.subLanguage);
         final HighlightParser parser = new HighlightParser(language, mRendererFactory, renderer);
         relevance = parser.highlight(mModeBuffer, true, mContinuations.get(mTop.mode.subLanguage));
         resultCode = renderer.getResult();
         resultLanguage = mTop.mode.subLanguage;
         mContinuations.put(mTop.mode.subLanguage, parser.mTop);
      }
      else {
         final Highlighter highlighter = new Highlighter(mRendererFactory);
         final Highlighter.HighlightResult result = highlighter.highlightAuto(mModeBuffer, mTop.mode.subLanguages);
         relevance = result.getRelevance();
         resultCode = result.getResult();
         resultLanguage = result.getLanguage();
      }

      // Counting embedded language score towards the host language may be disabled
      // with zeroing the containing mode relevance. Usecase in point is Markdown that
      // allows XML everywhere and makes every XML snippet to have a much larger Markdown
      // score.
      if (mTop.mode.relevance > 0) {
         mRelevance += relevance;
      }
      mBlockRenderer.onPushSubLanguage(resultLanguage, resultCode);
   }

   private int processLexeme(String buffer, String lexeme) throws Exception {
      mModeBuffer += buffer;

      if (lexeme == null) {
         processBuffer();
         return 0;
      }

      final Mode newMode = subMode(lexeme, mTop.mode);
      if (newMode != null) {
         if (newMode.skip) {
            mModeBuffer += lexeme;
         }
         else {
            if (newMode.excludeBegin) {
               mModeBuffer += lexeme;
            }
            processBuffer();
            if (!newMode.returnBegin && !newMode.excludeBegin) {
               mModeBuffer = lexeme;
            }
         }
         startNewMode(newMode);
         return newMode.returnBegin ? 0 : lexeme.length();
      }

      final ParentWrapper endMode = endOfMode(mTop, lexeme);
      if (endMode != null) {
         Mode origin = mTop.mode;
         if (origin.skip) {
            mModeBuffer += lexeme;
         }
         else {
            if (!(origin.returnEnd || origin.excludeEnd)) {
               mModeBuffer += lexeme;
            }
            processBuffer();
            if (origin.excludeEnd) {
               mModeBuffer = lexeme;
            }
         }

         do {
            if (mTop.mode.className != null) {
               mBlockRenderer.onPopStyle();
            }
            if (!mTop.mode.skip && mTop.mode.subLanguage == null) {
               mRelevance += mTop.mode.relevance;
            }
            mTop = mTop.parent;
         }
         while (mTop != endMode.parent);

         if (endMode.mode.starts != null) {
            startNewMode(endMode.mode.starts);
         }
         return origin.returnEnd ? 0 : lexeme.length();
      }

      if (isIllegal(lexeme, mTop.mode)) {
         throw new Exception("Illegal lexeme \"" + lexeme + "\" for mode \"" + mTop.mode.className + "\"");
      }

      /*
      Parser should not reach this point as all types of lexemes should be caught
      earlier, but if it does due to some bug make sure it advances at least one
      character forward to prevent infinite looping.
      */
      mModeBuffer += lexeme;
      return lexeme.length() > 0 ? lexeme.length() : 1;
   }

   int highlight(String code, boolean ignoreIllegals, ParentWrapper continuation) {
      try {
         mBlockRenderer.onStart();
         mLanguage.compile();
         mModeBuffer = "";
         mIgnoreIllegals = ignoreIllegals;
         mRelevance = 0;
         mTop = continuation == null ? new ParentWrapper(mLanguage, null) : continuation;
         mContinuations = new HashMap<>();

         final ArrayList<String> stack = new ArrayList<>();
         for (ParentWrapper current = mTop; current != null && current.mode != mLanguage; current = current.parent) {
            if (current.mode.className != null) {
               stack.add(0, current.mode.className);
            }
         }
         for (String className : stack) {
            mBlockRenderer.onPushStyle(className);
         }


         int index = 0;
         while (true) {
            if (mTop.mode.terminators != null) {
               final Matcher matcher = mTop.mode.terminators.matcher(code);
               if (!matcher.find(index)) {
                  break;
               }
               final int count = processLexeme(code.substring(index, matcher.start()), matcher.group());
               index = matcher.start() + count;
            }
            else {
               break;
            }
         }
         processLexeme(code.substring(index), null);
         for (ParentWrapper current = mTop; current.parent != null; current = current.parent) {
            if (current.mode.className != null) {
               mBlockRenderer.onPopStyle();
            }
         }
         mBlockRenderer.onFinish();
      }
      catch (Exception e) {
         mBlockRenderer.onAbort(code);
         mRelevance = 0;
      }

      return mRelevance;
   }
}
