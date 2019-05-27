package common;
/**
 * Created by jjzi on 2015/9/30.
 */
public class StatefulInteger extends Number {

    private int state;

    @Override
    public boolean equals(Object o) {
        return o instanceof StatefulInteger && state == ((StatefulInteger) o).state;
    }

    @Override
    public int hashCode() {
        return state;
    }

    public StatefulInteger(int initState) {
        this.state = initState;
    }

    @Override
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
