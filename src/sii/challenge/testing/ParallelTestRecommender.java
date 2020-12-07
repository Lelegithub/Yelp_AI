package sii.challenge.testing;

import java.util.*;
import java.util.concurrent.*;

import sii.challenge.IRecommender;
import sii.challenge.domain.*;
import sii.challenge.repository.*;

/**
 * Esegue in parallelo il Recommender di Testing
 * @author Arancia Rossa, Giampiero Pippi 
 *
 */
public class ParallelTestRecommender implements IRecommender {
        
        public List<MovieRating> recommend(List<MovieRating> input) throws Exception
        {
                List<MovieRating> output = new ArrayList<MovieRating>();
                
                int taskcount = Runtime.getRuntime().availableProcessors()*2;
                int inputlistsize = input.size();
                List<Future<List<MovieRating>>> tasks = new ArrayList<Future<List<MovieRating>>>(taskcount);
                ExecutorService pool = Executors.newFixedThreadPool(taskcount);
                for(int i = 0; i < taskcount; i++){
                        System.out.println("Starting Recommender"+i);
                        tasks.add(pool.submit(new TestRecommender(new K3SetRepository(), input.subList(inputlistsize/taskcount*i, inputlistsize/taskcount*(i+1)))));
                }
                                
                for(Future<List<MovieRating>> t : tasks) 
                        output.addAll(t.get());
                
                pool.shutdown();
                
                return output;
        }
        
        
}