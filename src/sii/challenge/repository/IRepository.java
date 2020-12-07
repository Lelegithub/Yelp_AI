package sii.challenge.repository;

import java.sql.Connection;
import java.util.List;

import sii.challenge.domain.MovieRating;

/**
 * 
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public interface IRepository {

        float getSingleFloatValue(String query, int[] args) throws Exception;
        float getSingleFloatValue(String query, int[] args, Connection connection) throws Exception;
        
        float getSingleFloatValue(String query, Object[] args) throws Exception;
        float getSingleFloatValue(String query, Object[] args, Connection connection) throws Exception;

        List<MovieRating> getMovieRatingList(String query, int[] args) throws Exception;

        void write(String query, Object[] args) throws Exception;
        void write(String query, Object[] args, Connection connection) throws Exception;
        
        List<Integer> getMovieIDs() throws Exception;
}