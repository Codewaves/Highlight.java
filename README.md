[![Download](https://api.bintray.com/packages/codewaves/maven/codehighlight/images/download.svg?version=1.0.2) ](https://bintray.com/codewaves/maven/codehighlight/1.0.2/link)
[![Build Status](https://travis-ci.org/Codewaves/Highlight.java.svg?branch=master)](https://travis-ci.org/Codewaves/Highlight.java)

# Highlight.java
Java port of popular Highlight.js library

## Download

Available on JCenter, grab via Gradle:
```groovy
compile 'com.codewaves.codehighlight:codehighlight:1.0.2'
```
or Maven:
```xml
<dependency>
  <groupId>com.codewaves.codehighlight</groupId>
  <artifactId>codehighlight</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```

## Usage

1. Implement StyleRenderer interface. You can use HtmlRenderer as example. Style renderers 
apply style to the code lexemes. In html output it can be <span> tags. In Android, *Span classes
applied to SpannableString.
2. Implement StyleRendererFactory interface to create renderer class instances.
3. Create Highlighter and use highlightAuto or highlight method to do actual work.

## Basic example

Basic example code that returns Highlight.js compatible output:
``` java
public class RendererFactory implements StyleRendererFactory {
   public StyleRenderer create(String languageName) {
      return new HtmlRenderer("hljs-");
   }
}
```
``` java
final Highlighter highlighter = new Highlighter(new RendererFactory());
final Highlighter.HighlightResult result = highlighter.highlightAuto(<source code>, null);
final CharSequence styledCode = result.getResult():
```

## Thread safety

Highlighter class is not thread safe. You must create different Highlighter class instance 
for every thread. 

## Author, License, and Copyright

Highlight.java is written and maintained by Sergej Kravcenko.

This library is based on Highlight.js (https://github.com/isagalaev/highlight.js).

    Copyright (c) 2017, Sergej Kravcenko
    Copyright (c) 2006, Ivan Sagalaev
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of highlight.js nor the names of its contributors
          may be used to endorse or promote products derived from this software
          without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.