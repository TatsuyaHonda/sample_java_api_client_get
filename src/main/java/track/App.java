package track;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public class App {

    private static String seed;
    private static Map<Integer, Integer> apiCache = new HashMap<>();

    public static void main(String[] args) {
        validateArgs(args);

        seed = args[0];
        int targetInt = Integer.parseInt(args[1]);
        int calculatedValue = calculateAsRecursive(targetInt);

        System.out.println(calculatedValue);
    }

    private static void validateArgs(String[] args) {
        if (Objects.isNull(args)) {
            System.out.println("第一引数、第二引数は必須パラメータです");
            System.exit(1);
        }
        if (args.length < 2) {
            System.out.println("第一引数、第二引数は必須パラメータです");
            System.exit(1);
        }
        if (args.length > 2) {
            System.out.println("第三引数以上は指定できません");
            System.exit(1);
        }
        String tmpSeed = args[0];
        String tmpInt = args[1];
        if (Objects.isNull(tmpSeed) || tmpSeed.isEmpty()) {
            System.out.println("第一引数が設定されていません");
            System.exit(1);
        }
        if (Objects.isNull(tmpInt) || tmpInt.isEmpty()) {
            System.out.println("第二引数が設定されていません");
            System.exit(1);
        }

        try {
            Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println("第二引数に整数以外の値が設定されています");
            System.exit(1);
        }
    }

    /**
     * n を計算する再帰関数
     * 
     * @param targetInt n
     * @return 計算結果
     */
    private static int calculateAsRecursive(int targetInt) {
        if (targetInt < 0)
            return 0; // INFO: 問題文からは読み解け無いため仮置きの値 0 としています

        if (targetInt == 0)
            return 1;

        if (targetInt == 2)
            return 2;

        if (targetInt % 2 == 0) {
            return (calculateAsRecursive(targetInt - 1) + calculateAsRecursive(targetInt - 2)
                    + calculateAsRecursive(targetInt - 3) + calculateAsRecursive(targetInt - 4));
        } else {
            return askServer(targetInt);
        }
    }

    /**
     * キャッシュが存在する場合はローカルのキャッシュを使用する
     * キャッシュに存在しない場合は、APIをコールする
     */
    private static int askServer(int targetInt) {
        if (apiCache.containsKey(targetInt))
            return apiCache.get(targetInt);

        int apiResult = callRecursiveAskAPI(seed, targetInt);
        apiCache.put(targetInt, apiResult);
        return apiResult;
    }

    /**
     * 提供されている計算APIをコールする
     * 
     * @param seedParam 引数のseed
     * @param targetInt 対象のn
     * @return APIResponse.result
     */
    private static int callRecursiveAskAPI(String seedParam, int targetInt) {
        int apiValue = 0;
        try {
            String url = String.format(
                    "http://challenge-server.code-check.io/api/recursive/ask?n=%s&seed=%s", targetInt, seedParam);

            HttpRequest request = null;
            request = HttpRequest.newBuilder(new URI(url))
                    .GET()
                    .build();

            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpResponse<String> response = null;
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            Response res = new ObjectMapper().readValue(body, Response.class);

            apiValue = Integer.parseInt(res.getResult());

            // INFO: 本来であればHTTPステータス等のチェックを行いますが、簡略化しています
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return apiValue;
    }
}
