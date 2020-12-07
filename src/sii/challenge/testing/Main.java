package sii.challenge.testing;


/**
 * Crea un nuovo CrossValidator ed esegue il relativo runTest
 * @author Arancia Rossa, Giampiero Pippi 
 *
 */

public class Main {

        public static void main(String[] args) {
                System.out.println("Starting Main...");
                
                CrossValidator crossvalidator = new CrossValidator();
                
                System.out.println("Starting CrossValidation...");
                float mae = crossvalidator.runTest();
                System.out.println("Cross Validator returned MAE " + mae);
                
        }

}