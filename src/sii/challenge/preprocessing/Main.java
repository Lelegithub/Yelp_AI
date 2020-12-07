package sii.challenge.preprocessing;

import sii.challenge.preprocessing.MultithreadPreprocessor;

/**
 * Lancia il preprocessamento per effettuare la similarità statica tra i movie presenti nel database, in base a quanto descritto in Mul
 * @author Arancia Rossa, Giampiero Pippi 
 *
 */
public class Main {

        public static void main(String[] args) {
                System.out.println("Starting Preprocessing...");
                
                (new Thread(new MultithreadPreprocessor(0, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(1, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(2, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(3, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(4, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(5, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(6, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(7, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(8, 10, 5105, 999999999))).start();
                (new Thread(new MultithreadPreprocessor(9, 10, 5105, 999999999))).start();
                
        }

}