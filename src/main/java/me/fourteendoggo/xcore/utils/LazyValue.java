package me.fourteendoggo.xcore.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class LazyValue<T> {
    private T value;
    protected final Supplier<T> supplier;
    protected final Predicate<T> validChecker;

    public LazyValue(Supplier<T> supplier, Predicate<T> validChecker) {
        this.supplier = supplier;
        this.validChecker = validChecker;
    }

    public T get() {
        // value is null or invalid, (re)compute
        if (value == null || (validChecker != null && !validChecker.test(value))) {
            value = supplier.get();
        }
        return value;
    }
}
