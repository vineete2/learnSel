package src.day04;
import java.util.HashMap;

public class hashmaplearn {

        public static void main(String[] args) {
            HashMap<String, String> capitalCities = new HashMap<String, String>();
            capitalCities.put("England", "London");
            capitalCities.put("Germany", "Berlin");
            capitalCities.put("Norway", "Oslo");
            capitalCities.put("USA", "Washington DC");
           // System.out.println(capitalCities);
            int[] nums = {1, 2, 9, 3,4,7,8};
            System.out.println(hasDuplicate(nums));
            Boolean t= hasDuplicate(nums);



            }


    private static boolean hasDuplicate(int[] nums) {
        HashMap<Integer, Integer> counter = new HashMap<Integer, Integer>();

        for(int i = 0; i<nums.length; i++)
        {
            int count=1;
            if(counter.containsKey(nums[i]))
                count++;
            counter.put(nums[i],count);
            if (counter.get(nums[i]) != null && counter.get(nums[i]) > 1) {
                return true;
            }
        }
        //System.out.println(counter);

        return false;
    }


}

