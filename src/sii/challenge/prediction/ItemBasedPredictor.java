package sii.challenge.prediction;

import sii.challenge.repository.IRepository;

/**
 * Definisce il classico approccio Item-Based NN; necessario il preprocessamento delle similarità tra gli utenti con la Adjusted Cosine Similarity definita come funzione in MySQL 
 * per aumentare la velocità di elaborazione.
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class ItemBasedPredictor implements IPredictor {

        private final IRepository repository;

        /**
         * Costruttore
         * @param repository
         */
        public ItemBasedPredictor(IRepository repository)
        {
                this.repository = repository;
        }
        

        @Override
        public float PredictRating(int userid, int movieid, long timestamp) {
                float p = 0;
                try {
                        p = this.repository.getSingleFloatValue(
                                        
                                        "SELECT SUM(pearsoncorr*rating)/SUM(pearsoncorr)" +
                                        "FROM (" +
                                                        "SELECT A.I1, A.I2, item_pearson(A.I1,A.I2) as pearsoncorr, B.rating, B.movieID" +
                                                        "FROM" +
                                                        " (" +
                                                        " SELECT DISTINCT URM1.movieID AS I1, URM2.movieID AS I2" +
                                                        " FROM user_ratedmovies URM1 JOIN user_ratedmovies URM2 " +
                                                        " ON URM1.movieID= ? AND URM2.movieID<>URM1.movieID AND URM1.userID=URM2.userID" +
                                                        " AND ABS(DATEDIFF(FROM_UNIXTIME(URM1.timestamp/1000), FROM_UNIXTIME(URM2.timestamp/1000))) < 7" +
                                                        " ) AS A" +
                                                        " JOIN" +
                                                        " (" +
                                                        " SELECT URM.rating, URM.movieID" +
                                                        " FROM user_ratedmovies URM" +
                                                        " WHERE URM.userID=? AND URM.movieID <> ?" +
                                                        " )AS B" +
                                                        " ON A.I1 = ? and B.movieID = A.I2" +
                                        " ) as PRED",
                                        
                                        new int[]{ movieid, userid, movieid, movieid } );
                        
                } catch (Exception e) {
                        return 0;
                }
                
                return p;
        }

        
}