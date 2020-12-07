package sii.challenge.testing;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import sii.challenge.IRecommender;
import sii.challenge.domain.*;
import sii.challenge.prediction.*;
import sii.challenge.repository.IRepository;

/**
 * Crea il Recommender per il test, costituito da un array di predittori, di predizioni, di errori di predizione, di MAE. In questo modo si eseguono contemporaneamente tre predittori.
 * Pertanto se uno fallisce, ossia se la predizione determinata è nulla, subentra il predittore successivo (tecnica di fallback)
 * 
 * @author Arancia Rossa, Giampiero Pippi 
 *
 */
public class TestRecommender implements Callable<List<MovieRating>>, IRecommender {

        private List<MovieRating> inputlist;
        
        private IPredictor[] predictors;
        private float[] predictions;
        private float[] predictorErrors;
        private float[] predictorMAEs;

        public TestRecommender(IRepository repository, List<MovieRating> input) {
                this(repository);
                
                this.inputlist = input;
        }
        
        /**
         * Costruttore
         * @param repository
         */
        public TestRecommender(IRepository repository)
        {
                System.out.println("R - Creating Predictor(s)...");
                this.predictors = new IPredictor[]{
                        new ExistingRatingPredictor(repository)
                        ,new MatrixFactorizationPredictor(repository)
                        ,new SimpleBiasPredictor(repository)
                        //,new DumbUserPredictor(repository)
                        //,new ItemTagBasedPredictor(repository)
                        //,new ItemGenreBasedPredictor(repository)
                        //,new SimpleTimeDependentBiasPredictor(repository)
                };
                this.predictions = new float[this.predictors.length];
                this.predictorErrors = new float[this.predictors.length];
                this.predictorMAEs = new float[this.predictors.length];
        }
        
        /**
         * Effettua la predizione adottando i predittori che sono stati assegnati al recommender, effettuando un arrotondamento di questi valori
         * per rapportarsi alla scala di voti da 0.5 a 5 con passo di 0.5.
         * @param input lista di movie rating che devono essere raccomandati e di cui si vuole conoresce il rating
         * @return una lista di oggetti MovieRating contentente i rating relative alle tuple passate in input
         * @throws Exception
         */
        public List<MovieRating> recommend(List<MovieRating> input) throws Exception
        {
                int i = 1;
                int c = input.size();
                System.out.println("R - Recommending...");
                List<MovieRating> ratings = new LinkedList<MovieRating>();
                
                float exp;
                float p;
                float roundedpred;
                float err;
                float totalerror = 0;
                
                for(int pi = 0; pi<this.predictors.length; pi++)
                        System.out.print(this.predictors[pi].getClass().getSimpleName() + "\t");
                System.out.println();
                
                for(MovieRating mr : input)
                {
                        exp =  mr.getRating();
                        p = 0;
                        
                        System.out.printf("%5s/%s:\t",i,c);
                        for(int pi = 0; pi<this.predictors.length; pi++)
                        {
                                this.predictions[pi] = this.predictors[pi].PredictRating(mr.getUserId(), mr.getMovieId(), mr.getTimestamp());
                                roundedpred = .5F*Math.round(this.predictions[pi]/.5);
                                this.predictorErrors[pi] += Math.abs(exp-roundedpred/*this.predictions[pi]*/);
                                this.predictorMAEs[pi] = this.predictorErrors[pi]/i;
                                System.out.printf("%1.1f|%5.1f|%1.8f\t", Math.abs(exp-roundedpred), this.predictorErrors[pi], this.predictorMAEs[pi]);
                                p += this.predictions[pi];
                        }

                        // predizione totale
                        p = this.predictions[0] > 0 ? this.predictions[0] : ( 
                                        this.predictions[1] > 0 ? this.predictions[1] : ( 
                                                this.predictions[2]
                                        )
                                );
                        p = .5F*Math.round(p/.5);

                        err = Math.abs(exp-p);
                        totalerror += err;
                        
                        System.out.println("]=> P: " + p + "\tERR: " + err + "\tMAE: " + totalerror/i);
                        
                        //if(p<0) throw new Exception("Prediction is less then zero.");
                        if(p<0) p = .5F;
                        
                        MovieRating pmr = new MovieRating(
                                mr.getUserId(), 
                                mr.getMovieId(), 
                                mr.getTimestamp(), 
                                .5F*Math.round(p/.5)
                        );
                        ratings.add(pmr);
                        
                        i++;
                }

                System.out.println("Total MAE:");
                for(int pi = 0; pi<this.predictors.length; pi++)
                {
                        this.predictorMAEs[pi] = this.predictorErrors[pi]/c;
                        System.out.printf("\t\t%s : %1.8f MAE\n", this.predictors[pi].getClass().getSimpleName(), this.predictorMAEs[pi]);
                }
                
                System.out.println();
                
                return ratings;
        }

        @Override
        public List<MovieRating> call() throws Exception {
                if(this.inputlist == null) throw new Exception("Input list not initialized.");
                
                return this.recommend(this.inputlist);
        }
        
}