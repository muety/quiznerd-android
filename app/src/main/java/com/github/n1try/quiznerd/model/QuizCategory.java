package com.github.n1try.quiznerd.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public enum QuizCategory {
    ANDROID("Android"),
    CPP("C++"),
    CSHARP("C#"),
    HTML("HTML"),
    JAVA("Java"),
    JS("JavaScript"),
    PHP("PHP"),
    PYTHON("Python"),
    SWIFT("Swift");

    private String displayName;

    QuizCategory(String displayName) {
        this.displayName = displayName;
    }

    public static List<String> asList() {
        List<String> list = new ArrayList<>(values().length);
        for (QuizCategory c : values()) {
            list.add(c.getDisplayName());
        }
        return list;
    }
}
