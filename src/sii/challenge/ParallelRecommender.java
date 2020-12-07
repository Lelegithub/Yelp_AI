package sii.challenge;

import java.util.*;
import java.util.concurrent.*;

import sii.challenge.domain.*;
import sii.challenge.repository.*;

/**
 * Permette di parallelizzare il recommender in modo da aumentare le prestazioni in termini di tempi di risposta
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class ParallelRecommender implements IRecommender {
        
        public List<MovieRating> recommend(List<MovieRating> input) throws Exception
        {
                List<MovieRating> output = new ArrayList<MovieRating>();
        
                System.out.println("Recommending...");
                long start = System.currentTimeMillis();
                
                int taskcount = Runtime.getRuntime().availableProcessors();
                int inputlistsize = input.size();
                List<Future<List<MovieRating>>> tasks = new ArrayList<Future<List<MovieRating>>>(taskcount);
                ExecutorService pool = Executors.newFixedThreadPool(taskcount);
                for(int i = 0; i < taskcount; i++)
                        tasks.add(pool.submit(new Recommender(new K3SetRepository(), input.subList(inputlistsize/taskcount*i, inputlistsize/taskcount*(i+1)))));
                                
                for(Future<List<MovieRating>> t : tasks) 
                        output.addAll(t.get());
                
                pool.shutdown();
                long end = System.currentTimeMillis();

                System.out.println();
                System.out.println();
                System.out.println("Recommendation complete.");
                float elapsedSeconds = (end-start)/1000F;
                System.out.println(input.size() + " predictions in "+elapsedSeconds + " seconds ("+(input.size()/elapsedSeconds)+" predictions per second)");
                
                return output;
        }
        
        
}