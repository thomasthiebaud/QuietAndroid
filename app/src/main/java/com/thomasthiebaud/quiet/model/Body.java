package com.thomasthiebaud.quiet.model;

import java.util.HashMap;

/**
 * Created by thomasthiebaud on 5/14/16.
 */
public class Body extends HashMap<String, Object> {
    public Body add(String key, Object value) {
        this.put(key, value);
        return this;
    }
}
