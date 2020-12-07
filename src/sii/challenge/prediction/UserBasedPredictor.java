package sii.challenge.prediction;

import sii.challenge.repository.IRepository;
/**
 * Definisce il classico approccio User-Based NN; necessario il preprocessamento delle similarità tra gli utenti con la Adjusted Cosine Similarity definita come funzione in MySQL 
 * per aumentare la velocità di elaborazione.
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class UserBasedPredictor implements IPredictor {

        private final IRepository repository;

        /**
         * Costruttore
         * @param repository
         */
        public UserBasedPredictor(IRepository repository)
        {
                this.repository = repository;
        }
        

        @Override
        public float PredictRating(int userid, int movieid, long timestamp) {
                float p = 0;
                try {
                        p = this.repository.getSingleFloatValue(
                                        
                                        "SELECT biasU1 + SUM(pearsoncorr*(ABS(ratingU2 - biasU2)))/SUM(pearsoncorr)" +
                                        "FROM (" +
                                                "SELECT pearsoncorr, biasU1, biasU2, UR.rating as ratingU2" +
                                                        "FROM (" +
                                                        " SELECT A.U1, A.U2, user_pearson_correlation(A.U1,A.U2) as pearsoncorr, B.biasU1, B.biasU2" +
                                                        "FROM (" +
                                                                " SELECT DISTINCT URM1.userID AS U1, URM2.userID AS U2" +
                                                                " FROM user_ratedmovies URM1 JOIN user_ratedmovies URM2 ON URM1.userID= ? AND URM2.userID<>URM1.userID AND URM1.movieID=URM2.movieID"+
                                                                "AND ABS(DATEDIFF(FROM_UNIXTIME(URM1.timestamp/1000), FROM_UNIXTIME(URM2.timestamp/1000))) < 7" +
                                                ") AS A" +
                                                "JOIN (" +
                                                        "SELECT DISTINCT URM1.userID AS U1, AVG(URM1.rating) AS biasU1, URM2.userID AS U2, AVG(URM2.rating) AS biasU2" +
                                                        "FROM user_ratedmovies URM1 JOIN user_ratedmovies URM2 ON URM1.userID=? AND URM1.movieID <> ? AND URM1.userID <> URM2.userID" +
                                                                "GROUP BY  URM2.userID"+
                                                        ")AS B "+
                                                "ON A.U1 = B.U1 AND A.U2 = B.U2" +
                                                " ) as VAL" +
                                                "JOIN" +
                                                "user_ratedmovies UR" +
                                                "ON UR.userID = VAL.U2 AND UR.movieID = ?" +
                                                ") AS PRED",
                                        
                                        new int[]{ userid, userid, movieid, movieid } );
                        
                } catch (Exception e) {
                        return 0;
                }
                
                return p;
        }

}