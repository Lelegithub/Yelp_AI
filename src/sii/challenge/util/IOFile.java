package sii.challenge.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import sii.challenge.domain.MovieRating;

/**
 * Classe designata per la lettura del file contenente le tuple (user,movie,timestamp) di cui si vuole predire il rating, che saranno scritti in un file opportunamente creato.
 * @author Arancia Rossa, Giampiero Pippi
 *
 */
public class IOFile {

        /**
         * Legge il file dato dal percorso e contente le tuple (user,movie,timestamp) che devono essere esaminate
         * @param path percorso del file contente le tuple che devono essere lette
         * @return una lista di tipo MovieRating contenente le tuple presenti nel file
         * @throws Exception
         */
        public static List<MovieRating> leggiRigaSplit(String path) throws Exception {

                FileReader fileDat = new FileReader(path);
                BufferedReader bf = new BufferedReader(fileDat);

                String line = bf.readLine();
                List<MovieRating> movieRatingList = new LinkedList<MovieRating>();
                MovieRating mr;
                int uID;
                int mID;
                long ts;
                while (line != null) {
                        line = bf.readLine();
                        if (line != null) {
                                String s[] = line.split("\t");
                                uID = Integer.parseInt(s[0]);
                                mID = Integer.parseInt(s[1]);
                                ts = Long.parseLong(s[2]);
                                mr = new MovieRating(uID, mID, ts, 0);
                                movieRatingList.add(mr);
                        }
                }
                return movieRatingList;
        }

        /**
         * Crea un nuovo File nel percorso e col nome specificato. Se esiste, lo elimina e lo ricrea. 
         * @param fileOutPut
         * @throws Exception
         */
        public static void truncateOutputFile(String fileOutPut) throws Exception {
                File file = new File(fileOutPut);
                if(file.exists()) file.delete();
                file.createNewFile();
        }
        
        /**
         * Scrive in coda ad un file designato i rating definiti.
         * @param movieRatings lista di oggetti MovieRating di cui si è determinata la predizione
         * @param fileOutPut percorso in cui è presente il file in cui si deve scrivere
         * @throws Exception
         */
        public static void appendToOutputFile(List<MovieRating> movieRatings, String fileOutPut) throws Exception {

                FileWriter fstream = new FileWriter(fileOutPut);
                BufferedWriter out = new BufferedWriter(fstream);
                for (MovieRating mr : movieRatings)
                        out.write(mr.getUserId() + "\t" + mr.getMovieId() + "\t" + mr.getTimestamp() + "\t" + mr.getRating() + "\n");
                        //out.write(mr.getRating() + "\n");

                out.close();
        }

}