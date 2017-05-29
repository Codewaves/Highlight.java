package com.codewaves.codehighlight.renderer;

import com.codewaves.codehighlight.core.StyleRenderer;

/*
 * Created by Sergej Kravcenko on 5/16/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

/**
 * Basic HTML renderer similar to Highlight.js
 */
public class HtmlRenderer implements StyleRenderer {
   private String mPrefix;
   private String mResult;

   public HtmlRenderer(String prefix) {
      mPrefix = prefix;
   }

   @Override
   public void onStart() {
      mResult = "";
   }

   @Override
   public void onFinish() {
   }

   @Override
   public void onPushStyle(String style) {
      mResult += "<span class=\"" + mPrefix + style + "\">";
   }

   @Override
   public void onPopStyle() {
      mResult += "</span>";
   }

   @Override
   public void onPushCodeBlock(CharSequence block) {
      mResult += escape(block.toString());
   }

   @Override
   public void onPushSubLanguage(String name, CharSequence code) {
      mResult += "<span class=\"" + name + "\">" + code + "</span>";
   }

   @Override
   public void onAbort(CharSequence code) {
      mResult = code.toString();
   }

   @Override
   public CharSequence getResult() {
      return mResult;
   }

   private String escape(String code) {
      return code.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
   }
}
