package util;

import java.util.Objects;

public class Pair<T1, T2> {
    public T1 first_ = null;
    public T2 second_ = null;

    public Pair(T1 first, T2 second) {
        first_ = first;
        second_ = second;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Pair<?, ?> tmp = (Pair<?, ?>) obj;
        return Objects.equals(first_, tmp.first_) && Objects.equals(second_, tmp.second_);
    }
}