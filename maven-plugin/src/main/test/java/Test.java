import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by oleksiyp on 5/8/16.
 */
public class Test {

    @org.junit.Test
    public void testAbc() throws ExecutionException, InterruptedException {
        CompletableFuture<String> abcHash = CompletableFuture.completedFuture("abc");
        CompletableFuture<String> defHash = CompletableFuture.completedFuture("def");

        abcHash = abcHash.thenApply((a) -> wait(a, 500));
        defHash = defHash.thenApply((a) -> wait(a, 1000));

        abcHash.acceptEither(defHash, System.out::println);

        abcHash.thenCombineAsync(defHash,
                (abc, def) -> abc + def)
                .thenAccept(System.out::println)
                .get();
    }

    private <T> T wait(T a, int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return a;
    }
}
