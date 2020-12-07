package sii.challenge.prediction;

import sii.challenge.repository.IRepository;

/**
 * Come predizione usa la media pesata dei voti dati dall'utente UserID ai TopN (100) Movies più simili a MovieID.
 * La similarità fra i tag dei movie è precalcolata in base ai valori statici posseduti
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class ItemTagBasedPredictor implements IPredictor {

        private final IRepository repository;
        
        public ItemTagBasedPredictor(IRepository repository)
        {
                this.repository = repository;
        }
        
        @Override
        public float PredictRating(int userid, int movieid, long timestamp) {
                float p = 0;
                try {
                        p = this.repository.getSingleFloatValue(
                                        "SELECT SUM(URM.rating * (ISS.tags))/SUM(ISS.tags) FROM " +
                                        "(SELECT * FROM user_ratedmovies WHERE userID=? AND movieID<>?) URM " +
                                        "JOIN " +
                                        "(SELECT iditem2, tags FROM item_static_similarities WHERE iditem1=? ORDER BY tags DESC LIMIT 100) ISS " +
                                        "ON URM.movieID=ISS.iditem2", 
                                        new int[]{ userid, movieid, movieid } );
                        
                } catch (Exception e) {
                        return 0;
                }

                if(p<=0) {
                        p=2.5F;
                }
                
                return p;
        }

}