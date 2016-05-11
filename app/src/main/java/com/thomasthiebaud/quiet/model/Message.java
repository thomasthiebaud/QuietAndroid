package com.thomasthiebaud.quiet.model;

import com.thomasthiebaud.quiet.model.Content;

/**
 * Created by thomasthiebaud on 5/1/16.
 */
public class Message {
    private String message;
    private Content content;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }
}
