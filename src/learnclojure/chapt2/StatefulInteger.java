package learnclojure.chapt2;

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
        return 0;
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

    public static void main(String args[]){
        StatefulInteger five=new StatefulInteger(5);
        StatefulInteger six=new StatefulInteger(6);
        System.out.print( clojure.lang.Util.equiv(five,six));
    }
}
