package com.foo;

// class to be mutated
public class PrimeNumberChecker {
    public boolean isEvenNumber(int i) {
        if (i<0) {
            throw new IllegalArgumentException();
        }
        else {
            return i%2==0;
        }
    }
}
