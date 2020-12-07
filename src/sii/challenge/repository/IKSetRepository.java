package sii.challenge.repository;

import java.util.List;

import sii.challenge.domain.MovieRating;
/**
 * Interfaccia per l'utilizzo trasparente del Repository dividendo i dati in K set per l'applicazione della Cross Validation 
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public interface IKSetRepository extends IRepository {
        /**
         * Restiuisce il test set corrente sotto forma di lista di MovieRating 
         * che verrà utilizzata come set per il testing
         * @return una lista di MovieRating
         * @throws Exception
         */
        List<MovieRating> getTestSet() throws Exception;

        /**
         * Restituisce la grandezza del set
         * @return la dimensione del set
         */
        int getKSetSize();
        
        /**
         * Setta l'indice del set corrente 
         * @param index
         */
        void setCurrentSetIndex(int index);
        
}