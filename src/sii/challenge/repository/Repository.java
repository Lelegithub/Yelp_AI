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
 * Definisce il punto di accesso verso il DataBase permettendo di effettuare agilmente query complesse
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class Repository implements IRepository {

        private DataSource dataSource;
        private Connection persistentConnection;
        
        public Repository()
        {
                this.dataSource = new DataSource();
                try {
                        this.persistentConnection = this.dataSource.getConnection();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
        
        protected void finalize() throws Throwable {
            try {
                        try {
                                if (this.persistentConnection != null) this.persistentConnection.close();
                        } catch (SQLException e) {
                                throw new Exception(e.getMessage());
                        }
            } finally {
                super.finalize();
            }
        }
        
        /**
         * 
         * @param query
         * @param args
         * @return
         * @throws Exception
         */
        public float getSingleFloatValue(String query, int[] args) throws Exception
        {
                Connection connection = null;
                float result = 0;           
                try {
                        result = this.getSingleFloatValue(query, args, this.persistentConnection);
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
         * 
         * @param query
         * @param args
         * @param connection
         * @return
         * @throws Exception
         */
        public float getSingleFloatValue(String query, int[] args, Connection connection) throws Exception
        {
                PreparedStatement statement = null;
                ResultSet result = null;
                float res = 0;

                try {
                        statement = connection.prepareStatement(query);
                        for(int i = 0; i<args.length; i++) statement.setInt(i+1, args[i]);
                        
                        result = statement.executeQuery();

                        while (result.next()){
                                res = result.getFloat(1);
                        }
                } catch (SQLException e) {
                        throw new Exception(e.getMessage());
                } finally {
                        try {
                                if (statement != null) statement.close();
                        } catch (SQLException e) {
                                throw new Exception(e.getMessage());
                        }
                }
                
                return res;
        }
        
        /**
         * 
         * @param query
         * @param args
         * @return
         * @throws Exception
         */
        public float getSingleFloatValue(String query, Object[] args) throws Exception
        {
                Connection connection = null;
                float result = 0;
                
                try {
                        result = this.getSingleFloatValue(query, args, this.persistentConnection);
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
         * 
         * @param query
         * @param args
         * @param connection
         * @return
         * @throws Exception
         */
        public float getSingleFloatValue(String query, Object[] args, Connection connection) throws Exception
        {
                PreparedStatement statement = null;
                ResultSet result = null;
                float res = 0;

                try {
                        statement = connection.prepareStatement(query);
                        for(int i = 0; i<args.length; i++){
                                if(args[i].getClass().equals(Integer.class)) 
                                        statement.setInt(i+1, (int)args[i]);
                                else if(args[i].getClass().equals(Long.class)) 
                                        statement.setLong(i+1, (long)args[i]);
                        }
                        
                        result = statement.executeQuery();

                        while (result.next()){
                                res = result.getFloat(1);
                        }
                } catch (SQLException e) {
                        throw new Exception(e.getMessage());
                } finally {
                        try {
                                if (statement != null) statement.close();
                        } catch (SQLException e) {
                                throw new Exception(e.getMessage());
                        }
                }
                
                return res;
        }
        
        
        /**
         * 
         * @param query
         * @param args
         * @return
         * @throws Exception
         */
        public List<MovieRating> getMovieRatingList(String query, int[] args) throws Exception
        {
                Connection connection = this.dataSource.getConnection();
                PreparedStatement statement = null;
                List<MovieRating> ratings = new LinkedList<MovieRating>();
                ResultSet result = null;
                
                try {
                        statement = connection.prepareStatement(query);
                        for(int i = 0; i<args.length; i++) statement.setInt(i+1, args[i]);
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
        
        
        
        /**
         * 
         * @param query
         * @param args
         * @throws Exception
         */
        public void write(String query, Object[] args) throws Exception
        {
                this.write(query, args, this.persistentConnection);
        }
        
        /**
         * 
         * @param query
         * @param args
         * @param connection
         * @throws Exception
         */
        public void write(String query, Object[] args, Connection connection) throws Exception
        {
                PreparedStatement statement = null;

                try {
                        statement = connection.prepareStatement(query);
                        for(int i = 0; i<args.length; i++){
                                try{
                                if(args[i].getClass().equals(Integer.class)) 
                                        statement.setInt(i+1, (int)args[i]);
                                else if(args[i].getClass().equals(Long.class)) 
                                        statement.setLong(i+1, (long)args[i]);
                                else if(args[i].getClass().equals(Float.class)) 
                                        statement.setFloat(i+1, (float)args[i]);
                                else if(args[i].getClass().equals(Double.class)) 
                                        statement.setDouble(i+1, (double)args[i]);
                                }catch(Exception ex){
                                        ex.printStackTrace();
                                }
                        }
                        
                        statement.executeUpdate();
                } catch (SQLException e) {
                        throw new Exception(e.getMessage());
                } finally {
                        try {
                                if (statement != null) statement.close();
                        } catch (SQLException e) {
                                throw new Exception(e.getMessage());
                        }
                }
        }
        
        /**
         * 
         * @return
         * @throws Exception
         */
        public List<Integer> getMovieIDs() throws Exception
        {
                PreparedStatement statement = null;
                List<Integer> ids = new LinkedList<Integer>();
                ResultSet result = null;
                String query = "SELECT id FROM movies";
                
                try {
                        statement = this.persistentConnection.prepareStatement(query);
                        result = statement.executeQuery();

                        while (result.next()) 
                                ids.add(result.getInt(1));
                        
                } catch (SQLException e) {
                        throw new Exception(e.getMessage());
                } finally {
                        try {
                                if (statement != null) statement.close();
                        } catch (SQLException e) {
                                throw new Exception(e.getMessage());
                        }
                }
                
                return ids;
        }
        
}