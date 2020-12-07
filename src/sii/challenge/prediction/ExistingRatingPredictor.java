package sii.challenge.prediction;

import sii.challenge.repository.IRepository;

/**
 * Predittore che verifica se il rating associato alla tupla (user, movie, timestamp) esiste già nel DataBase. Se si restituisce quel valore altrimenti restituisce 0. 
 * Aumento della precisione e evita la necessità di effettuare predizioni inutili. 
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class ExistingRatingPredictor implements IPredictor {

        private final IRepository repository;
        
        /**
         * Costruttore
         * @param repository
         */
        public ExistingRatingPredictor(IRepository repository)
        {
                this.repository = repository;
        }
        
        @Override
        public float PredictRating(int userid, int movieid, long timestamp) {
                try {
                        return this.repository.getSingleFloatValue("select rating from user_ratedmovies where userID=? and movieID=? and timestamp=?", new Object[]{userid, movieid, timestamp});
                } catch (Exception e) {
                        return 0;
                }
        }

}