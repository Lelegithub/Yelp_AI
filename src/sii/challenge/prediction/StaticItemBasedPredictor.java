package sii.challenge.prediction;

import sii.challenge.repository.IRepository;

/**
 * Come predizione usa la media pesata dei voti dati dall'utente UserID ai TopN (100) Movies più simili a MovieID.
 * La similarità fra Movies è precalcolata in base ai valori statici posseduti, quali corrispondenze di attori, registi, generi, general ratings, ...
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class StaticItemBasedPredictor implements IPredictor {

        private final IRepository repository;
        
        public StaticItemBasedPredictor(IRepository repository)
        {
                this.repository = repository;
        }
        
        @Override
        public float PredictRating(int userid, int movieid, long timestamp) {
                float p = 0;
                try {
                        p = this.repository.getSingleFloatValue(
                                        "SELECT SUM(URM.rating * (ISS.similarity))/SUM(ISS.similarity) FROM " +
                                        "(SELECT * FROM user_ratedmovies WHERE userID=? AND movieID<>?) URM " +
                                        "JOIN " +
                                        "(SELECT iditem2, similarity FROM item_static_similarities WHERE iditem1=? ORDER BY similarity DESC LIMIT 100) ISS " +
                                        "ON URM.movieID=ISS.iditem2", 
                                        new int[]{ userid, movieid, movieid } );
                        
                } catch (Exception e) {
                        return 0;
                }
                
                return p;
        }

}
