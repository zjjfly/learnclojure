package learnclojure.chapt2;

import clojure.lang.Numbers;

/**
 * Created by jjzi on 2015/9/30.
 */
public class StatefulInteger extends Number {

    private int state;

    public boolean equals(Object o) {
        return o instanceof StatefulInteger && state == ((StatefulInteger) o).state;
    }

    public int hashCode() {
        return state;
    }

    public StatefulInteger(int initState) {
        this.state = initState;
    }

    public int intValue() {
        return state;
    }

    @Override
    public long longValue() {
        return (long) state;
    }

    @Override
    public float floatValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return 0;
    }

    public void setInt(int newState) {
        this.state = newState;
    }
}
