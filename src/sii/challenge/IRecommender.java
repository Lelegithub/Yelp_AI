package sii.challenge;

import java.util.List;

import sii.challenge.domain.MovieRating;

/**
 * Interfaccia che deve essere implementata da tutti i recommender che si decide di implementare
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public interface IRecommender {
        
        /**
         * Effettua la predizione adottando i predittori che sono stati assegnati al recommender, effettuando un arrotondamento di questi valori
         * per rapportarsi alla scala di voti da 0.5 a 5 con passo di 0.5.
         * @param input Lista di movie rating che devono essere raccomandati e di cui si vuole conoresce il rating
         * @return una lista di oggetti MovieRating contentente i rating relative alle tuple passate in input
         * @throws Exception
         */
        List<MovieRating> recommend(List<MovieRating> input) throws Exception;
}
