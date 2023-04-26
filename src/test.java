import java.lang.Math;
import edu.ufl.cise.plcsp23.runtime.ConsoleIO;
public class test {
    public static String apply(int i01, int i11) {
        while(((int) Math.pow(i01 , i11 )) != 0) {
            return "aa" ;
        }
        ;
        return "bb" ;
    }

    public static void main(String[] args) {
        System.out.println(apply(1 , 2 ));
    }
}