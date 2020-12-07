package sii.challenge.preprocessing;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Estende la classe Preprocessor per effettuare il preprocessamento dei dati per la similarità statica tra movie sfruttando più thread paralleli.
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class MultithreadPreprocessor extends Preprocessor implements Runnable {

        private int tot;
        private int mod;
        private int id1lessthan;
        private int id1greaterthan;
        
        /**
         * Custruttore
         * @param mod
         * @param tot
         * @param id1greaterthan id del movie da cui iniziare il preprocessamento
         * @param id1lessthan id di fine per il preprocessamento
         */
        public MultithreadPreprocessor(int mod, int tot, int id1greaterthan, int id1lessthan)
        {
                super();
                this.mod = mod;
                this.tot = tot;
                this.id1lessthan = id1lessthan;
                this.id1greaterthan = id1greaterthan;
        }
        
        @Override 
        public void run()
        {
                try {
                        this.preprocess();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
        


        protected void preprocessItemStaticSimilarity() throws Exception
        {
                List<Integer> extmovieids;
                List<Integer> intmovieids;
                
                System.out.println("["+this.mod+"] ID1\tID2\tACT\tDIR\tGEN\tCOU\tTAG\tALL\tTOP\tAUD\tDEC\t\tSIM");

                this.connection = this.dataSource.getConnection();
                
                extmovieids = this.getIncompleteMovieIDs(this.id1greaterthan, this.id1lessthan, this.tot, this.mod);
                
                for(int id1 : extmovieids)
                {
                        intmovieids = this.getRemainingInternalMovieIDs(id1);
                        for(int id2 : intmovieids) this.calculateAndPersistSimilarity(id1, id2);
                }

                try {
                        if (this.connection != null) this.connection.close();
                } catch (SQLException e) {
                        throw new Exception(e.getMessage());
                }
        }
        
        /**
         * Stampa a video gli id dei due movie, i relativi gradi di somiglianza e la similarità totale tra i due movie.
         * @param id1 id del primo movie
         * @param id2 id del secondo movie con cui si è fatto il confronto
         * @param actorsincommon numero di attori in comune
         * @param directorsincommon numero di registi in comune
         * @param genresincommon numero di generi in comune
         * @param countriesincommon numero di country in comune
         * @param tagsincommon numero di tag in comune
         * @param allcriticsscorediscrepance discrepanza tra gli allcriticsscore assegnati ai movie
         * @param topcriticsscorediscrepance discrepanza tra gli topcriticsscorediscrepance assegnati ai movie
         * @param audiencescorediscrepance discrepanza tra gli audiencescorediscrepance assegnati ai movie
         * @param decadediscrepance discrepanza tra le decadi dei due movie
         * @param similarity valore di similarità tra i due movie compresa tra [0,1]
         * @return una stringa contentenente le informazioni descritte
         */
        protected String formatSimilarityLogLine(int id1, int id2, float actorsincommon, float directorsincommon, float genresincommon, float countriesincommon, float tagsincommon, float allcriticsscorediscrepance, float topcriticsscorediscrepance, float audiencescorediscrepance, float decadediscrepance, float similarity) {
                return "["+this.mod+"] " + super.formatSimilarityLogLine(id1, id2, actorsincommon, directorsincommon, genresincommon, countriesincommon, tagsincommon, allcriticsscorediscrepance, topcriticsscorediscrepance, audiencescorediscrepance, decadediscrepance, similarity);
        }

        /**
         * Utilizzato per completare il preprocessamento e verificare quali sono i movie che devono essere ancora preprocessati e che non sono ancora all'interno della tabella
         * item_static_similarity
         * @param AfterIDIncluded
         * @param BeforeIDExcluded
         * @param tot
         * @param mod
         * @return
         * @throws Exception
         */
        protected List<Integer> getIncompleteMovieIDs(int AfterIDIncluded, int BeforeIDExcluded, int tot, int mod) throws Exception
        {
                PreparedStatement statement = null;
                List<Integer> ids = new LinkedList<Integer>();
                ResultSet result = null;
                String query = "SELECT M.id FROM (SELECT id from movies where id>=? AND id<? AND id % ? = ?) as M LEFT JOIN (select iditem1 from item_static_similarities where iditem1>=? AND iditem1<? AND iditem1 % ? = ? GROUP BY iditem1 HAVING COUNT(iditem2)=10197) as S ON M.id = S.iditem1 WHERE S.iditem1 IS NULL"; 
        
                try {
                        statement = this.connection.prepareStatement(query);
                        statement.setInt(1, AfterIDIncluded);
                        statement.setInt(2, BeforeIDExcluded);
                        statement.setInt(3, tot);
                        statement.setInt(4, mod);
                        statement.setInt(5, AfterIDIncluded);
                        statement.setInt(6, BeforeIDExcluded);
                        statement.setInt(7, tot);
                        statement.setInt(8, mod);
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
}