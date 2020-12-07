package sii.challenge.matrixFactorization;

import java.util.*;

import Jama.Matrix;

import sii.challenge.domain.MovieRating;
import sii.challenge.repository.IRepository;

/**
 * Effettua le conversioni delle strutture dati per il preprocessamento della matrix factorization da e verso il database
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class MatrixFactorizationDataAdapter {
        
        Map<Integer, Integer> j2movie = new HashMap<Integer, Integer>();
        Map<Integer, Integer> i2user = new HashMap<Integer, Integer>();
        
        IRepository repository; 
        
        public MatrixFactorizationDataAdapter(IRepository repository){
                this.repository=repository;
        }
        
        
        
        
        
        public IRepository getRepository() {
                return repository;
        }
        public void setRepository(IRepository repository) {
                this.repository = repository;
        }

        public Map<Integer, Integer> getJ2movie() {
                return j2movie;
        }
        public void setJ2movie(Map<Integer, Integer> j2movie) {
                this.j2movie = j2movie;
        }

        public Map<Integer, Integer> getI2user() {
                return i2user;
        }
        public void setI2user(Map<Integer, Integer> i2user) {
                this.i2user = i2user;
        }


        /**
         * Prende i rating dal DB e crea una matrice UxI->R utile alla MatrixFactorization
         * @return una matrice
         * @throws Exception
         */
        public Matrix readAndAdapt() throws Exception {

                Map<Integer, Integer> movie2j = new HashMap<Integer, Integer>();
                Map<Integer, Integer> user2i = new HashMap<Integer, Integer>();
                
                int usercount = (int)repository.getSingleFloatValue("SELECT COUNT(DISTINCT userID) FROM user_ratedmovies", new int[]{});
                //int moviecount = (int)repository.getSingleFloatValue("SELECT COUNT(DISTINCT movieID) FROM user_ratedmovies", new int[]{});
                int moviecount = (int)repository.getSingleFloatValue("SELECT COUNT(*) FROM movies", new int[]{});
                
                List<MovieRating> ratings = repository.getMovieRatingList("SELECT * from user_ratedmovies", new int[]{});

                
                Matrix matrix = new Matrix(usercount, moviecount);

                int mi, ui;
                List<Integer> movieIDs = repository.getMovieIDs();
                for(int i = 0; i<movieIDs.size(); i++) {
                        int mid = movieIDs.get(i);
                        movie2j.put(mid, i);
                        j2movie.put(i, mid);
                }

                int u = 0;
                //int m = 0;
                for(MovieRating rating : ratings) {
                        
                        if(user2i.containsKey(rating.getUserId()))
                                ui = user2i.get(rating.getUserId());
                        else{
                                ui = u;
                                user2i.put(rating.getUserId(), ui);
                                i2user.put(ui, rating.getUserId());
                                u++;
                        }
                        /*if(movie2j.containsKey(rating.getMovieId()))
                                mi = movie2j.get(rating.getMovieId());
                        else{
                                mi = m;
                                movie2j.put(rating.getMovieId(), mi);
                                j2movie.put(mi, rating.getMovieId());
                                m++;
                        }*/
                        mi = movie2j.get(rating.getMovieId());
                        
                        matrix.set(ui, mi, rating.getRating());
                }
                
                return matrix;
        }
        
        /**
         * Converte la matrice risultante dalla fattorizzazione in tuple da scrivere sul database
         * @param matrix
         * @param offseti
         * @param offsetj
         * @throws Exception
         */
        public void adaptAndWrite(Matrix matrix, int offseti, int offsetj) throws Exception {
                Object[] param = new Object[matrix.getRowDimension()*matrix.getRowDimension()*3];
                StringBuilder query = new StringBuilder();
                query.append("INSERT INTO predictionmatrix (userID, movieID, rating) VALUES");
                for(int i = offseti, ri=0; i<matrix.getRowDimension()+offseti; i++, ri++)
                        for(int j = offsetj, rj=0; j<matrix.getColumnDimension()+offsetj; j++, rj++)
                        {
                                query.append(" (?,?,?),");
                                int baseparamindex = ((ri * matrix.getRowDimension()) + rj)*3;
                                param[baseparamindex] = i2user.get(i);
                                param[baseparamindex+1] = j2movie.get(j);
                                param[baseparamindex+2] = matrix.get(ri, rj);
                                //this.repository.write(query, new Object[]{ i2user.get(i), j2movie.get(j), matrix.get(i-offseti, j-offsetj) });
                        }
                
                String q = query.toString();
                q = q.substring(0, q.length()-1);
                this.repository.write(q, param);
        }
        
}