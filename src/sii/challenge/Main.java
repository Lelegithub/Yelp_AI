package sii.challenge;

import java.util.List;

import sii.challenge.domain.MovieRating;
import sii.challenge.util.IOFile;


/**
 * Lancia l'esecuzione del sistema
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class Main {
        
        /**
         * Legge i path dei file da cui si desidera leggere e in cui si vuole scrivere i rating che sono stati predetti
         * e lancia l'esecuzione di ParalleRecommender
         * @param args
         * @throws Exception
         */
        public static void main(String[] args) throws Exception {

                System.out.println("SIIChallenge (Arancia Rossa, Giampiero Pippi)");
                
                if(args.length<2) {

                        System.out.println("Usage: siichallenge.jar <inputfile> <outputfile>");
                        
                } else {
                
                        String inputfilename = args[0];
                        String outputfilename = args[1];
                        
                        
                        List<MovieRating> inputList = IOFile.leggiRigaSplit(inputfilename);
                        
                        IOFile.truncateOutputFile(outputfilename);
                        
                        List<MovieRating> predictions = new ParallelRecommender().recommend(inputList);
                        IOFile.appendToOutputFile(predictions, outputfilename);
        
                        System.out.println("Grazie per aver usato il nostro Sistema di Raccomandazione!\nA presto.");
                }
        }

}