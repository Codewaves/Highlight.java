package com.codewaves.codehighlight.core;

/**
 * Created by Sergej Kravcenko on 5/16/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

/**
 *
 */
public interface StyleRendererFactory {
   /**
    *
    * @param languageName
    * @return
    */
   StyleRenderer create(String languageName);
}
