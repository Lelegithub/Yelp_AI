package sii.challenge.prediction;

/**
 * Interfaccia contenente il metodo PredictRating che deve essere implementato da tutti i predittori che vengono definiti
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public interface IPredictor {
        
        /**
         * Dato l'id dell'user, del movie ed il relativo timestamp, perdette di ottenere la predizione del rating relativa a questa tupla
         * @param userid
         * @param movieid
         * @param timestamp
         * @return un valore numerico di tipo float che rappresenta il rating per la tripla passata come input
         */
        public float PredictRating(int userid, int movieid, long timestamp);
        
}