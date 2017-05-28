package com.codewaves.codehighlight.core;

/**
 * Created by Sergej Kravcenko on 5/17/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class TestRenderer implements StyleRenderer {
   private String mResult;

   @Override
   public void onStart() {
      mResult = "";
   }

   @Override
   public void onFinish() {
   }

   @Override
   public void onPushStyle(String style) {
      mResult += "<span class=\"hljs-" + style + "\">";
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
