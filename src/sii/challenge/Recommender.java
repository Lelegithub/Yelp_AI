package sii.challenge;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import sii.challenge.domain.*;
import sii.challenge.prediction.*;
import sii.challenge.repository.IRepository;

/**
 * Implementa l'oggetto principale che effettua le predizioni.
 * Vengono creati una serie di predittori, usati in cascata con tecniche di fallback qualora il precedente non trovi predizioni attendibili.
 * 
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class Recommender implements Callable<List<MovieRating>>, IRecommender {

        private List<MovieRating> inputlist;
        
        private IPredictor[] predictors;
        
        /**
         * Costruttore
         * @param repository
         * @param input
         */
        public Recommender(IRepository repository, List<MovieRating> input) {
                this(repository);
                
                this.inputlist = input;
        }
        
        /**
         * Inizializza il recommendere con una lista di predittori da utilizzare per l'applicazione.
         * I predittori adottati sono:ExistingRatingPredictor, MatrixFactorizationPredictor, SimpleBiasPredictor
         * @param repository
         */
        public Recommender(IRepository repository)
        {
                this.predictors = new IPredictor[]{
                        new ExistingRatingPredictor(repository),
                        new MatrixFactorizationPredictor(repository),
                        new SimpleTimeDependentBiasPredictor(repository)
                };
        }
        
        @Override
        public List<MovieRating> recommend(List<MovieRating> input) throws Exception
        {
                float p = 0;
                
                List<MovieRating> ratings = new LinkedList<MovieRating>();
                
                for(MovieRating mr : input)
                {
                        for(IPredictor predictor : this.predictors)
                        {
                                p = predictor.PredictRating(mr.getUserId(), mr.getMovieId(), mr.getTimestamp());
                                if(p>0) break;
                        }
                        
                        // predizione finale
                        p = .5F*Math.round(p/.5);
                        
                        if(p<0) p = 3F;
                        
                        MovieRating pmr = new MovieRating(
                                mr.getUserId(), 
                                mr.getMovieId(), 
                                mr.getTimestamp(), 
                                p
                        );
                        ratings.add(pmr);
                        
                        System.out.print(".");
                }
                
                return ratings;
        }

        @Override
        public List<MovieRating> call() throws Exception {
                if(this.inputlist == null) throw new Exception("Input list not initialized.");
                
                return this.recommend(this.inputlist);
        }
        
}