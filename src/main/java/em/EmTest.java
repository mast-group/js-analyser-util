package em;

/**
 * Created by Pankajan on 25/01/2016.
 */
public class EmTest {
    public static void main(String[] args) {
        double[][] data = new double[1][30];
        for(int i=0; i<10; i++) {
            data[0][i] = i+1;
        }
        for(int i=0; i<10; i++) {
            data[0][10+i] = 50+i;
        }
        for(int i=0; i<10; i++) {
            data[0][20+i] = 100*(i+1);
        }
        for (double value : data[0]) {
            System.out.print(value + ", ") ;
        }

        ExpMax em = new ExpMax(data, 1, 3);
        em.calculateParameters();
//        em.printParameters();
    }
}
