package com.company;

import java.io.*;
import java.util.*;
import java.lang.String ;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
	// write your code here
        int minSupportCount = 0 , minConfidence = 0 ;
        Scanner scan = new Scanner ( System.in );

        System.out.println ( "Enter minSupport and minConfidence in that order" );
        minSupportCount = scan.nextInt ();
        minConfidence = scan.nextInt ();

        scan = new Scanner ( System.in );


        ArrayList<ArrayList<String>> records = read("C:\\Users\\Mohamed Nasr\\Downloads\\Assignment1\\CoffeeShopTransactions.csv");
        APrioriAlgorithm ( records , minSupportCount , minConfidence );
    }





    private static void APrioriAlgorithm ( ArrayList<ArrayList<String>> records , int minSupportCount , int minConfidence ) {
        ArrayList<String> distinctValues = getDistinctValues ( records );

        int itemSetSize = 0 ;
        ArrayList<ArrayList<String>> prevItemSetsWithMinSupportCount = new ArrayList<ArrayList<String>>();

        while (true) {

            itemSetSize++;
            ArrayList<Integer> supportCountList = new ArrayList<Integer>();

            ArrayList<ArrayList<String>> itemSets = getItemSets(distinctValues, itemSetSize);

            //calculate support_count
            for (ArrayList<String> itemSet : itemSets){
                int count = 0 ;
                for (ArrayList<String> record : records){
                    if (existsInTransaction ( itemSet , record )){
                        count ++ ;
                    }
                }
                supportCountList.add ( count );
            }

            ArrayList<ArrayList<String>> itemsWithSupportCountEqualOrExceedMinSupportCount = getItemSetsWithMinSupportCount(itemSets , supportCountList , minSupportCount);
            if (itemsWithSupportCountEqualOrExceedMinSupportCount.size() == 0) {
                System.out.print("Most Frequent ItemSets : ");
                System.out.println(prevItemSetsWithMinSupportCount);

                getAssociationRulesAndCalcConfidence ( prevItemSetsWithMinSupportCount , records , minConfidence);

                break;
            }
            distinctValues = getDistinctValues (itemsWithSupportCountEqualOrExceedMinSupportCount);

            prevItemSetsWithMinSupportCount = itemsWithSupportCountEqualOrExceedMinSupportCount;
        }
    }

    private static void getAssociationRulesAndCalcConfidence(ArrayList<ArrayList<String>> frequentItemSets, ArrayList<ArrayList<String>> records, int minConfidence) {
        for (ArrayList<String> itemSet : frequentItemSets) {
            if (itemSet.size() > 1) {
                ArrayList<ArrayList<String>> subsets = getNonEmptySubsets(itemSet);

                for (ArrayList<String> subset : subsets) {
                    ArrayList<String> remainingItems = new ArrayList<>(itemSet);
                    remainingItems.removeAll(subset);

                    int supportItemSet = getSupportCount(records, itemSet);
                    int supportSubset = getSupportCount(records, subset);

                    if (supportItemSet > 0 && supportSubset > 0) {
                        double confidence = (double) supportItemSet / supportSubset;

                        if (confidence >= (double) minConfidence / 100) {
                            System.out.println(subset + " => " + remainingItems + " (Conf: " + confidence + ")");
                        }
                    }
                }
            }
        }
    }

    private static ArrayList<ArrayList<String>> getNonEmptySubsets(ArrayList<String> itemSet) {
        ArrayList<ArrayList<String>> subsets = new ArrayList<>();

        for (int i = 1; i < Math.pow(2, itemSet.size()) - 1; i++) {
            ArrayList<String> subset = new ArrayList<>();

            for (int j = 0; j < itemSet.size(); j++) {
                if ((i & (1 << j)) != 0) {
                    subset.add(itemSet.get(j));
                }
            }

            subsets.add(subset);
        }
        return subsets;
    }

    private static int getSupportCount(ArrayList<ArrayList<String>> records, ArrayList<String> items) {
        int count = 0;

        for (ArrayList<String> record : records) {
            if (existsInTransaction(items, record)) {
                count++;
            }
        }

        return count;
    }


    private static ArrayList< ArrayList< String>> getItemSets ( ArrayList< String> distinctItems , int itemSetSize ) {
        if (itemSetSize ==1){
            ArrayList<ArrayList<String>> itemSets = new ArrayList <> ( );

            for (String item : distinctItems){
                ArrayList<String> toArrayList = new ArrayList <> (  );
                toArrayList.add ( item );
                itemSets.add ( toArrayList );
            }
            return itemSets ;
        }else{
            int size = distinctItems.size();

            ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();

            for (int i = 0; i < size; i++) {

                // Copy items to _items
                ArrayList < String > _items = new ArrayList < String > ( distinctItems );

                // Get item at i-th position
                String thisItem = distinctItems.get(i);

                // Remove items upTo i, inclusive
                _items.subList ( 0 , i + 1 ).clear ( );

                // Get permutations of the remaining items
                ArrayList<ArrayList<String>> permutationsBelow = getItemSets(_items, itemSetSize - 1);

                // Add thisItem to each permutation and add the permutation to toReturn
                for (ArrayList<String> aList : permutationsBelow) {
                    aList.add(thisItem);
                    Collections.sort (aList );
                    toReturn.add(aList);
                }
            }
            return toReturn;
        }
    }

    public static ArrayList<ArrayList<String>> read ( String filePath ){
        int calc = 0 ;
        BufferedReader reader = null;
        String line  = "";
        String[] row;
        ArrayList<ArrayList<String>> records = new ArrayList <> (  );

        try {
            reader = new BufferedReader(new FileReader( filePath ));

            while((line = reader.readLine()) != null) {
                ArrayList<String> x = new ArrayList<>();
                if (calc != 0) {
                    row = line.split ( "," );
                    x.add(row[3]);
                    x.add(row[4]);
                    x.add(row[5]);
                    records.add ( x );
                }else{
                    calc++;
                }

            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert reader != null;
                reader.close ( );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return records ;
    }

    public static ArrayList<String>  getDistinctValues (ArrayList<ArrayList<String>> records){
        ArrayList<String> distinctValues = new ArrayList <> (  );
        for (int i = 0 ; i < records.size () ; i++){
            for (int j = 0 ; j < records.get ( i ).size () ; j++){
                if (distinctValues.size () == 0){
                    distinctValues.add ( records.get ( i ).get ( j ));
                }else {
                    if (!distinctValues.contains ( records.get ( i ).get ( j ) )){
                        distinctValues.add ( records.get ( i ).get ( j ));
                    }
                }
            }
        }
        return distinctValues ;
    }

    private static boolean existsInTransaction (ArrayList<String> items, ArrayList<String> transaction) {
        for (String item : items) {
            if (!transaction.contains(item)) return false;
        }
        return true;
    }

    private static ArrayList<ArrayList<String>> getItemSetsWithMinSupportCount ( ArrayList<ArrayList<String>> itemSets, ArrayList<Integer> countList, int minSupportCount) {

        ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();

        for (int i = 0; i < countList.size(); i++) {
            int count = countList.get(i);
            if (count >= minSupportCount) {
                toReturn.add(itemSets.get(i));
            }
        }

        return toReturn;
    }
}
