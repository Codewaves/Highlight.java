package com.codewaves.codehighlight.core;

/*
 * Created by Sergej Kravcenko on 5/16/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

/**
 * Interface definition for callbacks to be invoked when the code
 * is being parsed for syntax highlight.
 */
public interface StyleRenderer {
   /**
    * Callback method to be invoked when parser starts code processing.
    */
   void onStart();

   /**
    * Callback method to be invoked when parser finishes code processing.
    */
   void onFinish();

   /**
    * Callback method to be invoked when parser finds start of lexeme.
    *
    * @param style span style name
    */
   void onPushStyle(String style);

   /**
    * Callback method to be invoked when parser finds end of lexeme.
    */
   void onPopStyle();

   /**
    * Callback method to be invoked when parser finds complete code
    * lexeme. Code lexeme style is specified by last {@link StyleRenderer#onPushStyle(String)}
    * call.
    *
    * @param codeLexeme code lexeme
    */
   void onPushCodeBlock(CharSequence codeLexeme);

   /**
    * Callback method to be invoked when parser finds sub-language block inside the
    * code.
    *
    * @param name language name
    * @param code code block processed and highlighted by another {@link StyleRenderer}
    */
   void onPushSubLanguage(String name, CharSequence code);

   /**
    * Callback method to be invoked when parser cannot continue code processing due
    * an error.
    *
    * @param code code string passed to {@link Highlighter#highlight(String, String)} method
    */
   void onAbort(CharSequence code);

   /**
    * Returns result of code highlighting.
    *
    * @return highlighted code
    */
   CharSequence getResult();
}
