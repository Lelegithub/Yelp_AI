package sii.challenge.matrixFactorization;

import java.util.LinkedList;
import java.util.List;

import Jama.*;

/**
 * Effettua la fattorizzazione di una matrice attreverso la regolarizzazione dei parametri.
 * @author Arancia Rossa, Giampiero Pippi
 */
public class MatrixFactorizer {
        
        final static int steps = 5000;
        final static float alpha = 0.0002F;
        final static float beta = 0.02F;
                        
        /**
         * Effettua l'aggiornamento delle matrici P e Q minimizzando l'error quadratico tra il rating reale e quello predetto
         * @param R matrice di rating 
         * @param P matrice relativa alle feature dell'user
         * @param Q matrice relativa alle feature del movie
         * @param K nummero di feature
         * @param step numero di ere
         * @param alpha learning rate
         * @param beta fattore di regolarizzazione
         * @return una lista di tipo Matrix contente le matrici P e Q
         */
        public static List<Matrix> factorize(Matrix R, Matrix P, Matrix Q, int K,int step, float alpha, float beta) {
                
                List<Matrix> feature = new LinkedList<Matrix>();
                Q = Q.transpose();
                double eij, dotVal;
                //Matrix eR;
                int colP = P.getColumnDimension();
                int rowsQ = Q.getRowDimension();
                for(int s=0; s<step ; s++ ){
                        for(int i=0; i<R.getRowDimension(); i++ ) {
                                for(int j=0; j<R.getColumnDimension(); j++ ) {
                                        
                                        //System.out.print(".");
                        
                                        if(R.get(i, j)>0){
                                                dotVal = dot(P.getMatrix(i, i, 0, colP-1),Q.getMatrix(0, rowsQ-1, j, j)).get(0, 0);
                                                // eij è l'errore (!) ottenuto come differenza tra il rating reale e quello predetto!
                                                eij = R.get(i, j) - dotVal;     
                                                for(int k=0; k<K; k++){
                                                        double pik = P.get(i,k);
                                                        double qkj = Q.get(k,j);
                                                        pik = pik + alpha * (2 * eij * qkj - beta * pik);
                                                        P.set(i, k, pik); 
                                                        qkj = qkj + alpha * (2 * eij * P.get(i,k) - beta * qkj);
                                                        Q.set(k, j, qkj);
                                                }       
                                        }
                                }

                        }
                double e = 0;
                for(int i=0; i<R.getRowDimension(); i++ )
                                for(int j=0; j<R.getColumnDimension(); j++ )
                                        if(R.get(i,j)>0){
                                                dotVal = dot(P.getMatrix(i, i, 0, K-1),Q.getMatrix(0, K-1, j, j)).get(0, 0);
                                                e = e + Math.pow(R.get(i,j) - dotVal, 2);                                           
                                                for(int k=0; k<K; k++)                                        
                                                        e = e + (beta/2) * ( Math.pow(P.get(i,k),2) + Math.pow(Q.get(k,j),2));
                                            
                                        }
                                        
                if(e < 0.001){
                        break;
                }
                }
                
                Q= Q.transpose();
        feature.add(P);
        feature.add(Q); 
                
        return feature;
        }

        /**
         * Date due matrici ritorna il prodotto matriciale di queste
         * @param P
         * @param Q
         * @return il prodotto matriciale
         */
        public static Matrix dot(Matrix P, Matrix Q){
                return P.times(Q);
        }
        
        /**
         * Stampa a video la matrice passata come parametro
         * @param R matrice
         */
        public static void printMatrix(Matrix R)
        {       
                for(int i=0; i<R.getRowDimension(); i++){
                        for(int j=0; j<R.getColumnDimension(); j++)
                                System.out.print(R.get(i, j)+ " ");
                        System.out.println();
                }
                System.out.println();
                System.out.println();
        }


}