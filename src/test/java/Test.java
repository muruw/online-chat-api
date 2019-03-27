

public class Test {

    public static void main(String[] args) throws Exception {

        SayHello helloObject = new SayHello();
        assertThat("Tervitus peab algama suure tähega", helloObject.hello("Hello") == 1);
        assertThat("Tervitus peab algama suure tähega", helloObject.hello("hello") == -1);

        assertThat("Tervitus peab algama suure tähega", helloObject.hello("HELLO") == 1);
        System.out.println("Passed the tests");
    }
    
    static void assertThat(String description, boolean isCorrect) throws Exception{
        if(!isCorrect){
            throw new Exception(description);
        }

    }
}
