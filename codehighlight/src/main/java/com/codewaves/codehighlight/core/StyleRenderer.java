package com.codewaves.codehighlight.core;

/**
 * Created by Sergej Kravcenko on 5/16/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

/**
 *
 */
public interface StyleRenderer {
   void onStart();
   void onFinish();
   void onPushStyle(String style);
   void onPopStyle();
   void onPushCodeBlock(CharSequence code);
   void onPushSubLanguage(String name, CharSequence code);
   void onAbort(CharSequence code);
   CharSequence getResult();
}
