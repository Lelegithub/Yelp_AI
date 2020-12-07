package sii.challenge.preprocessing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import sii.challenge.repository.*;
import sii.challenge.util.DataSource;

/**
 * Permette di effettuare il preprocessamento dei dati per la similarità statica tra movie
 * @author Arancia Rossa, Giampiero Pippi 
 *
 */
public class Preprocessor {

        protected DataSource dataSource;
        protected Connection connection;
        protected IRepository repository;
        
        /**
         * Costruttore inizializzato con DataSource e Repository
         */
        public Preprocessor()
        {
                this.dataSource = new DataSource();
                this.repository = new Repository();
        }
        
        /**
         * Esegue preprocessItemStaticSimilarity()
         * @throws Exception
         */
        public void preprocess() throws Exception
        {
                this.preprocessItemStaticSimilarity();

                System.out.println("Preprocessing complete.");
        }
        
        /**
         * Effettua il preprocessamento dei dati passando al metodo calculateAndPersist gli id che devono essere processati, presenti nella lista movieids
         * @throws Exception
         */
        protected void preprocessItemStaticSimilarity() throws Exception
        {
                System.out.println("ID1\tID2\tACT\tDIR\tGEN\tCOU\tTAG\tALL\tTOP\tAUD\tDEC\t\tSIM");

                this.connection = this.dataSource.getConnection();
                
                List<Integer> movieids = this.getMovieIDs();
                
                for(int id1 : movieids)
                        for(int id2 : movieids)
                                calculateAndPersistSimilarity(id1, id2);

                try {
                        if (this.connection != null) this.connection.close();
                } catch (SQLException e) {
                        throw new Exception(e.getMessage());
                }
        }

        /**
         * Calcola e persiste nella tabella item_static_similarities del database il grado di similarità tra i due film basata su: 
         * actorsincommon, directorsincommon, genresincommon, countriesincommon, tagsincommon, allcriticsscorediscrepance, topcriticsscorediscrepance, 
         * audiencescorediscrepance, decadediscrepance. I valori di queste componenti sono comprese tra [0,1].
         * La similarità tra due movie è quindi ottenuta attraverso una cobinazione degli elementi suddetti opportunamenti pesati restuendo un valore compreso tra [0,1]
         *  
         * @param id1: id del primo movie
         * @param id2: id del secondo movie 
         * @throws Exception
         */
        protected void calculateAndPersistSimilarity(int id1, int id2) throws Exception {
                float actorsincommon = this.getStuffInCommon("movie_actors", "actorID", id1, id2);
                float directorsincommon = this.getStuffInCommon("movie_directors", "directorID", id1, id2);
                float genresincommon = this.getStuffInCommon("movie_genres", "genre", id1, id2);
                float countriesincommon = this.getStuffInCommon("movie_countries", "country", id1, id2);
                float tagsincommon = this.getWeightedStuffInCommon("movie_tags", "tagID", "normalizedTagWeight", id1, id2);
                float allcriticsscorediscrepance = this.getDiscrepance("movies", "rtAllCriticsScore", 100, id1, id2);
                float topcriticsscorediscrepance = this.getDiscrepance("movies", "rtTopCriticsScore", 100, id1, id2);
                float audiencescorediscrepance = this.getDiscrepance("movies", "rtAudienceScore", 100, id1, id2);
                float decadediscrepance = this.getDecadeDiscrepance(id1, id2);
                float similarity = 
                                actorsincommon * 6 + 
                                directorsincommon * 3 + 
                                genresincommon * 9 + 
                                countriesincommon * 3 + 
                                tagsincommon * 5 +
                                allcriticsscorediscrepance * 5 + 
                                topcriticsscorediscrepance * 5 + 
                                audiencescorediscrepance * 5 + 
                                decadediscrepance * 5;
                similarity /= 46;
                
                System.out.println(formatSimilarityLogLine(id1, id2, actorsincommon, directorsincommon, genresincommon, countriesincommon, tagsincommon, allcriticsscorediscrepance, topcriticsscorediscrepance, audiencescorediscrepance, decadediscrepance, similarity));
                
                PreparedStatement statement = null;
                String query = "INSERT INTO item_static_similarities (iditem1, iditem2, actors, directors, genres, countries, tags, allcriticsscore, topcriticsscore, audiencescore, decadediscrepance, similarity) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                
                try {
                        
                        statement = this.connection.prepareStatement(query);
                        statement.setInt(1, id1);
                        statement.setInt(2, id2);
                        statement.setFloat(3, actorsincommon);
                        statement.setFloat(4, directorsincommon);
                        statement.setFloat(5, genresincommon);
                        statement.setFloat(6, countriesincommon);
                        statement.setFloat(7, tagsincommon);
                        statement.setFloat(8, allcriticsscorediscrepance);
                        statement.setFloat(9, topcriticsscorediscrepance);
                        statement.setFloat(10, audiencescorediscrepance);
                        statement.setFloat(11, decadediscrepance);
                        statement.setFloat(12, similarity);
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
         * Stampa a video gli id dei due movie, i relativi gradi di somiglianza e la similarità totale tra i due movie.
         * @param id1: id del primo movie
         * @param id2: id del secondo movie con cui si è fatto il confronto
         * @param actorsincommon: numero di attori in comune
         * @param directorsincommon: numero di registi in comune
         * @param genresincommon: numero di generi in comune
         * @param countriesincommon: numero di country in comune
         * @param tagsincommon: numero di tag in comune
         * @param allcriticsscorediscrepance: discrepanza tra gli allcriticsscore assegnati ai movie
         * @param topcriticsscorediscrepance: discrepanza tra gli topcriticsscorediscrepance assegnati ai movie
         * @param audiencescorediscrepance: discrepanza tra gli audiencescorediscrepance assegnati ai movie
         * @param decadediscrepance: discrepanza tra le decadi dei due movie
         * @param similarity: valore di similarità tra i due movie compresa tra [0,1]
         * @return una stringa
         */
        protected String formatSimilarityLogLine(int id1, int id2, float actorsincommon, float directorsincommon, float genresincommon, float countriesincommon, float tagsincommon, float allcriticsscorediscrepance, float topcriticsscorediscrepance, float audiencescorediscrepance, float decadediscrepance, float similarity) {
                return id1+"\t"+id2+"\t"+actorsincommon+"\t"+directorsincommon+"\t"+genresincommon+"\t"+countriesincommon+"\t"+tagsincommon+"\t"+allcriticsscorediscrepance+"\t"+topcriticsscorediscrepance+"\t"+audiencescorediscrepance+"\t"+decadediscrepance+"\t\t"+similarity;
        }
        
        /**
         * Recupera dal DB la lista di id relativi ai movie
         * @return una lista di interi rappresentati gli id dei movie
         * @throws Exception
         */
        protected List<Integer> getMovieIDs() throws Exception
        {
                PreparedStatement statement = null;
                List<Integer> ids = new LinkedList<Integer>();
                ResultSet result = null;
                String query = "SELECT id FROM movies";
                
                try {
                        statement = this.connection.prepareStatement(query);
                        result = statement.executeQuery();

                        while (result.next()) {
                                ids.add(result.getInt(1));
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
                
                return ids;
        }
        
        /**
         * Determina quali sono gli id che non sono presenti nella tabella item_static_similarities.
         * Necessario a causa del lungo preprocessamento
         * @param iditem1
         * @return lista di interi rappresentanti id dei movie
         * @throws Exception
         */
        protected List<Integer> getRemainingInternalMovieIDs(int iditem1) throws Exception
        {
                PreparedStatement statement = null;
                List<Integer> ids = new LinkedList<Integer>();
                ResultSet result = null;
                String query = "SELECT id from movies where id not in (SELECT iditem2 FROM item_static_similarities WHERE iditem1=?)";
                
                try {
                        statement = this.connection.prepareStatement(query);
                        statement.setInt(1, iditem1);
                        result = statement.executeQuery();

                        while (result.next()) {
                                ids.add(result.getInt(1));
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
                
                return ids;
        }
        
        /**
         * Determina il numero di elementi in comune tra due movies in base alla colonna(ad esempio: idactor) e alla tabella (ad es: movie_actors).
         * @param tablename
         * @param idcolname
         * @param idmovie1
         * @param idmovie2
         * @return il numero di elementi in comune tra i due film
         * @throws Exception
         */
        protected float getStuffInCommon(String tablename, String idcolname, int idmovie1, int idmovie2) throws Exception
        {
                String query = "SELECT incommon/total as perc FROM " +
                                       "(SELECT COUNT(*) as incommon " +
                                           "FROM "+tablename+" A1 JOIN "+tablename+" A2 ON A1."+idcolname+"=A2."+idcolname + " " +
                                       "WHERE A1.movieID = ? AND A2.movieID =?) G1, " + 
                                           "(SELECT COUNT(*) as total " +
                                           "FROM "+tablename+" T1 " +
                                           "WHERE T1.movieID = ?) G2";
                
                return this.repository.getSingleFloatValue(query, new int[]{ idmovie1, idmovie2, idmovie1 }, this.connection);
        }

        
        /**
         * Grazie ai pesi associati ai tag in comune tra due movie, determina il grado di similarità tra questi in base al tag
         * @param tablename
         * @param idcolname
         * @param weightcolname
         * @param idmovie1
         * @param idmovie2
         * @return
         * @throws Exception
         */
        protected float getWeightedStuffInCommon(String tablename, String idcolname, String weightcolname, int idmovie1, int idmovie2) throws Exception
        {
                String query = "SELECT incommon/total as perc FROM " +
                                       "(SELECT SUM(1-ABS(A1."+weightcolname+"-A2."+weightcolname+")) as incommon " +
                                           "FROM "+tablename+" A1 JOIN "+tablename+" A2 ON A1."+idcolname+"=A2."+idcolname + " " +
                                       "WHERE A1.movieID = ? AND A2.movieID = ?) G1, " + 
                                           "(SELECT COUNT(*) as total " +
                                           "FROM "+tablename+" T1 " +
                                           "WHERE T1.movieID = ?) G2";
                
                return this.repository.getSingleFloatValue(query, new int[]{ idmovie1, idmovie2, idmovie1 }, this.connection);
                
        }
        
        /**
         * Determina la discrepanza tra due valori numerici relativi ai due movie in esame. 
         * Ad esempio la discrepanza tra audiencescore associata ai due movie passati come inpunt
         * @param tablename
         * @param colname: la colonna della tabella da cui ottenere i valori
         * @param normalizationfactor
         * @param idmovie1
         * @param idmovie2
         * @return la discrepanza tra i valori recuperati
         * @throws Exception
         */
        protected float getDiscrepance(String tablename, String colname, int normalizationfactor, int idmovie1, int idmovie2) throws Exception
        {
                String query = "SELECT 1-ABS((A1."+colname+" - A2."+colname+")/"+normalizationfactor+") as perc " +
                                           "FROM "+tablename+" A1, "+tablename+" A2 " +
                                       "WHERE A1.id = ? AND A2.id = ?";
                
                return this.repository.getSingleFloatValue(query, new int[]{ idmovie1, idmovie2 }, this.connection);
        }
        
        /**
         * Dati due movie determina la differenza di decadi che c'è tra questi
         * @param idmovie1
         * @param idmovie2
         * @return la discrepanza tra le due decadi
         * @throws Exception
         */
        protected float getDecadeDiscrepance(int idmovie1, int idmovie2) throws Exception
        {
                String query = "SELECT 1-(ABS(FLOOR(A1.year/10) - FLOOR(A2.year/10))/10) as y " +
                                           "FROM movies A1, movies A2 " +
                                       "WHERE A1.id = ? AND A2.id = ?";
                
                float val = this.repository.getSingleFloatValue(query, new int[]{ idmovie1, idmovie2 }, this.connection);
                return val>=0 ? val : 0;
        }
        
}