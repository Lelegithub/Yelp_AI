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
 * Implementa un Repository per la CrossValidation basata su Kset sfruttando una divisione statica di training set e test set
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class K3SetRepository extends Repository implements IKSetRepository {

        private DataSource dataSource;
        private int K;
        private int KSetSize;
        private int CurrentSetIndex;
        
        private int MovieRatingCount = 0;
        
        public K3SetRepository()
        {
                this.dataSource = new DataSource();
                
                try {
                        this.MovieRatingCount = (int)this.getSingleFloatValueWithoutQueryRewriting("SELECT COUNT(*) FROM user_ratedmovies", new int[]{});
                } catch (Exception e) {
                        e.printStackTrace();
                }
                
                this.K = 3;
                this.KSetSize = this.MovieRatingCount / this.K;
                this.CurrentSetIndex = 1;
        }
        
        public void setCurrentSetIndex(int index)
        {
                // fake!
        }
        
        public int getKSetSize()
        {
                return this.KSetSize;
        }
        

        
        private String getKView()
        {
                return "trainingset";
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
        
        
        /**
         * Restituisce il set dei dati attualmente utilizzato come Test Set
         * 
         * @return una lista di oggetti MovieRating
         * @throws Exception 
         */
        public List<MovieRating> getTestSet() throws Exception {
                Connection connection = this.dataSource.getConnection();
                PreparedStatement statement = null;
                List<MovieRating> ratings = new LinkedList<MovieRating>();
                ResultSet result = null;
                String query = "SELECT * FROM testset";

                int ksetsize = this.MovieRatingCount / this.K;
                int startrowindex = ksetsize*this.CurrentSetIndex;
                
                System.out.println("REP - TestSet goes from " + startrowindex + " to " + (startrowindex+ksetsize) + ".");
                
                try {
                        statement = connection.prepareStatement(query);
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