package sii.challenge.matrixFactorization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sii.challenge.repository.*;

import Jama.Matrix;

/**
 * Esegue il preprocessamento della matrice di rating sparsa iniziale tramite ParallelMatrixFactorizer
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class Main {
        


        public static void main(String[] args) throws Exception{

                MatrixFactorizationDataAdapter dataadapter = new MatrixFactorizationDataAdapter(new K3SetRepository());
                
                Matrix R = null;
                try {
                        R = dataadapter.readAndAdapt();
                } catch (Exception e) {
                        e.printStackTrace();
                }

                List<Future<?>> tasks = new ArrayList<Future<?>>(4);
                ExecutorService pool = Executors.newFixedThreadPool(4);
                for(int i = 0; i<2; i++)
                        for(int j = 0; j<2; j++)
                                tasks.add(pool.submit(new ParallelMatrixFactorizer(R, i%2==0, j%2==0, dataadapter.i2user, dataadapter.j2movie)));
                
                for(Future<?> t : tasks) {
                        t.get();
                }
                
                pool.shutdown();
                
        }

}