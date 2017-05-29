package com.codewaves.codehighlight.core;

/**
 * Created by Sergej Kravcenko on 5/14/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

public class Keyword {
   protected String value;
   protected String className;
   protected int relevance;

   public Keyword(String className, String value) {
      this.value = value;
      this.className = className;
   }

   public Keyword(String value, String className, int relevance) {
      this.value = value;
      this.className = className;
      this.relevance = relevance;
   }
}
