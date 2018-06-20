package com.github.n1try.quiznerd.model;

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
}
