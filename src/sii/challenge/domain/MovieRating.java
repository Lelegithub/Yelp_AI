package sii.challenge.domain;

/**
 * Classe per rappresentare le tuple (user, movie, timestamp, rating) 
 * 
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class MovieRating {

        private int userId;
        private int movieId;
        private long timestamp;
        private float rating;
        
        
        public MovieRating(int userId, int movieId, long timestamp, float rating) {
                super();
                this.userId = userId;
                this.movieId = movieId;
                this.timestamp = timestamp;
                this.rating = rating;
        }


        public int getUserId() {
                return userId;
        }


        public void setUserId(int userId) {
                this.userId = userId;
        }


        public int getMovieId() {
                return movieId;
        }


        public void setMovieId(int movieId) {
                this.movieId = movieId;
        }


        public long getTimestamp() {
                return timestamp;
        }


        public void setTimestamp(long timestamp) {
                this.timestamp = timestamp;
        }


        public float getRating() {
                return rating;
        }


        public void setRating(float rating) {
                this.rating = rating;
        }
        
        
        
}