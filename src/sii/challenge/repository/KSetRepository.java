package sii.challenge.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import sii.challenge.domain.MovieRating;
import sii.challenge.util.DataSource;

/**
 * Implementa un Repository per la CrossValidation basata su Kset determinando dinamicamente training set e test set
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class KSetRepository extends Repository implements IKSetRepository {

        private DataSource dataSource;
        private int K;
        private int KSetSize;
        private int CurrentSetIndex;
        
        private int MovieRatingCount = 0;
        
        public KSetRepository(int K)
        {
                this.dataSource = new DataSource();
                
                try {
                        this.MovieRatingCount = (int)this.getSingleFloatValueWithoutQueryRewriting("SELECT COUNT(*) FROM user_ratedmovies", new int[]{});
                } catch (Exception e) {
                        e.printStackTrace();
                }
                
                this.K = K;
                this.KSetSize = this.MovieRatingCount / this.K;
        }
        
        
        public void setCurrentSetIndex(int index)
        {
                this.CurrentSetIndex = index;
        }
        
        public int getKSetSize()
        {
                return this.KSetSize;
        }
        

        
        private String getKView()
        {
                int startrowindex = this.KSetSize*this.CurrentSetIndex - 1;
                int ithsetendingindex = this.KSetSize*(this.CurrentSetIndex+1);
                
                //return "((SELECT * FROM user_ratedmovies LIMIT 0, "+startrowindex+") UNION (SELECT * FROM user_ratedmovies LIMIT "+ithsetendingindex+", "+this.MovieRatingCount+")) AS KVIEW";
                return "(SELECT userID, movieID, rating, 'timestamp' FROM numbered_user_ratedmovies WHERE N<"+startrowindex+" OR N>="+ithsetendingindex+") AS KVIEW";
        }
        private String adaptQueryToCurrentKSet(String query)
        {
                return query.replaceAll("user_ratedmovies", this.getKView());
        }
        


        public float getSingleFloatValue(String query, int[] args, Connection connection) throws Exception
        {
                query = adaptQueryToCurrentKSet(query);
                return super.getSingleFloatValue(query, args, connection);
        }
        public float getSingleFloatValue(String query, Object[] args, Connection connection) throws Exception
        {
                query = adaptQueryToCurrentKSet(query);
                return super.getSingleFloatValue(query, args, connection);
        }
        public float getSingleFloatValueWithoutQueryRewriting(String query, int[] args) throws Exception
        {
                Connection connection = null;
                float result = 0;
                
                try {
                        connection = this.dataSource.getConnection();
                        result = super.getSingleFloatValue(query, args, connection);
                } catch(Exception e) {
                        
                } finally {
                        try {
                                if (connection != null) connection.close();
                        } catch (SQLException e) {
                                throw new Exception(e.getMessage());
                        }
                }
                
                return result;
        }
        
        

        public List<MovieRating> getMovieRatingList(String query, int[] args) throws Exception
        {
                query = adaptQueryToCurrentKSet(query);
                
                return super.getMovieRatingList(query, args);
        }
        

        public List<MovieRating> getTestSet() throws Exception {
                Connection connection = this.dataSource.getConnection();
                PreparedStatement statement = null;
                List<MovieRating> ratings = new LinkedList<MovieRating>();
                ResultSet result = null;
                String query = "SELECT * FROM numbered_user_ratedmovies WHERE N>? AND N<=?";

                int ksetsize = this.MovieRatingCount / this.K;
                int startrowindex = ksetsize*this.CurrentSetIndex;
                
                System.out.println("REP - TestSet goes from " + startrowindex + " to " + (startrowindex+ksetsize) + ".");
                
                try {
                        statement = connection.prepareStatement(query);
                        statement.setInt(1, startrowindex);
                        statement.setInt(2, startrowindex+ksetsize);
                        result = statement.executeQuery();

                        while (result.next()) {
                                MovieRating rating = new MovieRating(
                                                result.getInt("userID"),
                                                result.getInt("movieID"),
                                                result.getLong("timestamp"),
                                                result.getFloat("rating")
                                );
                                ratings.add(rating);
                        }
                } catch (SQLException e) {
                        throw new Exception(e.getMessage());
                } finally {
                        try {
                                if (statement != null)
                                        statement.close();
                                if (connection != null)
                                        connection.close();
                        } catch (SQLException e) {
                                throw new Exception(e.getMessage());
                        }
                }
                
                return ratings;
        }
        
}