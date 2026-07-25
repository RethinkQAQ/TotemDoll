package com.example.examplemod.doll.bone;

public record DollActionBinding(String id, String animation, String trigger, int priority,
                                int minInterval, int maxInterval) {}
