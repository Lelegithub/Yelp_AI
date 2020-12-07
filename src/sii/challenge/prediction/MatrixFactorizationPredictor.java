package sii.challenge.prediction;

import sii.challenge.repository.IRepository;

/**
 * Recupera dal database il rating per la tupla (user, movie, timestamp), che è stata precedentemente preprocessata tramite l'utilizzo del metodo factorize presente in MatrixFactorizer
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class MatrixFactorizationPredictor implements IPredictor {

        private final IRepository repository;
        
        /**
         * Costruttore
         * @param repository
         */
        public MatrixFactorizationPredictor(IRepository repository)
        {
                this.repository = repository;
        }
        
        @Override
        public float PredictRating(int userid, int movieid, long timestamp) {
                try {
                        return this.repository.getSingleFloatValue("select rating from predictionmatrix where userID=? and movieID=?", new int[]{userid, movieid});
                } catch (Exception e) {
                        return 0;
                }
        }

}