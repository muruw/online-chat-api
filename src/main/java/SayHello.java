public class SayHello {

    public int hello(String greeting) {
        String[] stringToList = greeting.split("");
        stringToList[0] = stringToList[0].toUpperCase();

        String newString = "";

        for (String character : stringToList) {
            newString += character;
        }
        System.out.println(newString);
        System.out.println(greeting);
        if (greeting.equals(newString)) {
            return 1;
        }
        return -1;
    }

}
