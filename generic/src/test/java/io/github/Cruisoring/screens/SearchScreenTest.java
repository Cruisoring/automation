package io.github.Cruisoring.screens;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.workers.Worker;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchScreenTest {

    String[] targetLinks = new String[] {
            "www.codeproject.com/Articles/1231137/%2fArticles%2f1231137%2ffunctionExtensions-Techniques-Throwable-Functional",
            "www.codeproject.com/Articles/1232570/Function-Extensions-Techniques-Tuples",
            "www.codeproject.com/Articles/1233122/%2fArticles%2f1233122%2ffunctionExtensions-Techniques-Repository",
            "github.com/Cruisoring/functionExtensions"
    };

    String[] keywords = new String[] {
            "function extensions java", "functional programming exceptional java", "java throwable functional interface", "java tuples closable", "throwable functions checked exception", "throwable method functional interface", "functionExtensions Techniques java"
    };

    Set<String> keywordSet = Arrays.stream(keywords)
            .flatMap(keys -> Arrays.stream(keys.split("[\\s,\\t]+")))
            .collect(Collectors.toSet());

    String getTitleColor(String title){
        long count = keywordSet.stream()
                .filter(key -> StringUtils.containsIgnoreCase(title, key)).count();
        if(count < 2)
            return null;
        else if (count < 3)
            return "lightgray";
        else if (count < 4)
            return "wheat";
        else if (count < 5)
            return "orange";
        return "purple";
    }

    boolean linkMatch(String url){
        return Arrays.stream(targetLinks).anyMatch(link -> StringUtils.containsIgnoreCase(url, link));
    }

    @Test
    public void searchGoogle() {
        try (Worker worker = Worker.create(DriverType.Chrome);){
            worker.gotoUrl("http://www.google.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.searchToLast(10, this::getTitleColor, this::linkMatch,  "functional programming exceptional java");
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void searchBaidu() {
        try (Worker worker = Worker.create(DriverType.Opera);){
            worker.gotoUrl("http://www.baidu.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.searchToLast(10, this::getTitleColor, this::linkMatch,  "cruisoring function extensions java");
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void searchBing() {
        try (Worker worker = Worker.create(DriverType.Edge);){
            worker.gotoUrl("http://www.bing.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.searchToLast(10, this::getTitleColor, this::linkMatch,  "github cruisoring repository");
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void searchAsk() {
        try (Worker worker = Worker.create(DriverType.Firefox);){
            worker.gotoUrl("http://www.ask.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.searchToLast(10, this::getTitleColor, this::linkMatch,  keywords);
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void searchAOL() {
        try (Worker worker = Worker.create(DriverType.Edge);){
            worker.gotoUrl("http://www.aol.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.searchToLast(10, this::getTitleColor, this::linkMatch,  keywords);
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    private Boolean runSearch(List<Worker> workers, int index,  String url){
        try (
                Worker worker = workers.get(index);
                ){
            worker.gotoUrl(url);

            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);
            searchScreen.searchToLast(10, this::getTitleColor, this::linkMatch,  keywords);
            Worker.close(worker);
            return true;
        }catch (Exception ex){
            Logger.W(ex);
            return false;
        } finally {
            workers.remove(index);
        }
    }

    @Test
    public void runMultiple(){
        List<Worker> workers = Worker.createAll(2, DriverType.Chrome, DriverType.Opera, DriverType.Edge, DriverType.Firefox, DriverType.Chrome);

        try {
            Executor.runParallel(5,
                    () -> runSearch(workers, 0, "http://www.google.com"),
                    () -> runSearch(workers, 1, "http://www.bing.com"),
                    () -> runSearch(workers, 2, "http://www.aol.com"),
                    () -> runSearch(workers, 3, "http://www.ask.com"),
                    () -> runSearch(workers, 4, "http://www.baidu.com")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}