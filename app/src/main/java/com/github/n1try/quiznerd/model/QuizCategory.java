package com.github.n1try.quiznerd.model;

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

    private final String displayName;

    QuizCategory(String displayName) {
        this.displayName = displayName;
    }

}
