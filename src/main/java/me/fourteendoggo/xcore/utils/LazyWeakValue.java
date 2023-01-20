package me.fourteendoggo.xcore.utils;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LazyWeakValue<T> extends LazyValue<T> {
    private WeakReference<T> value;

    public LazyWeakValue(Supplier<T> supplier, Predicate<T> validChecker) {
        super(supplier, validChecker);
    }

    @Override
    public T get() {
        // value is null or invalid, (re)compute
        if (value == null || (validChecker != null && !validChecker.test(value.get()))) {
            value = new WeakReference<>(supplier.get());
        }
        return value.get();
    }
}
