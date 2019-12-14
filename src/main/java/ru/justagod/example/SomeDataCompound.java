package ru.justagod.example;

public class SomeDataCompound extends PersistentUnit {
    private int a;
    public int b;
    private final int c;

    public SomeDataCompound() {
        c = 0;
    }

    public SomeDataCompound(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
