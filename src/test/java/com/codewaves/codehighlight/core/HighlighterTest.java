package com.codewaves.codehighlight.core;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by Sergej Kravcenko on 5/17/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */
public class HighlighterTest {
   @Test
   public void highlight() throws Exception {
      Highlighter highlighter = new Highlighter(new TestRendererFactory());
      Highlighter.HighlightResult result = highlighter.highlight("java", "import test;");

      assertTrue(result.getResult() != null);
   }

   @Test
   public void detection() throws Exception {
      // Get language examples directory
      File resourcesDirectory = new File("src/test/resources/detect");
      for (File lang : resourcesDirectory.listFiles()) {
         String name = lang.getName();
         // Only existing languages
         if (Highlighter.findLanguage(name) != null) {
            if (lang.isDirectory()) {
               for (File example : lang.listFiles()) {
                  String content = new String(Files.readAllBytes(example.toPath()));

                  Highlighter highlighter = new Highlighter(new TestRendererFactory());
                  Highlighter.HighlightResult result = highlighter.highlightAuto(content, null);
                  assertEquals(name, result.getLanguage());
               }
            }
         }
      }
   }

   @Test
   public void markup() throws Exception {
      // Get language examples directory
      File resourcesDirectory = new File("src/test/resources/markup");
      for (File lang : resourcesDirectory.listFiles()) {
         String name = lang.getName();

         // Only existing languages
         if (Highlighter.findLanguage(name) != null) {
            if (lang.isDirectory()) {
               for (File example : lang.listFiles()) {
                  String expectName = example.getPath();
                  if (expectName.contains(".expect.txt")) {
                     String contentName = expectName.replace(".expect", "");

                     String content = new String(Files.readAllBytes(Paths.get(contentName)));
                     String expect = new String(Files.readAllBytes(Paths.get(expectName)));

                     Highlighter highlighter = new Highlighter(new TestRendererFactory());
                     Highlighter.HighlightResult result = highlighter.highlight(name, content);
                     assertEquals(expect.trim(), result.getResult().toString().trim());
                  }
               }
            }
         }
      }
   }
}