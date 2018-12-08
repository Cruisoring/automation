package io.github.Cruisoring.screens;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.workers.Worker;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

public class SearchScreenTest {

    @Test
    public void searchGoogle() {
        try (Worker worker = Worker.create(DriverType.Chrome);){
            worker.gotoUrl("http://www.google.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.gotoLastPage("function extensions");
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void searchBaidu() {
        try (Worker worker = Worker.create(DriverType.IE);){
            worker.gotoUrl("http://www.baidu.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.gotoLastPage("function extensions");
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void searchBing() {
        try (Worker worker = Worker.create(DriverType.Edge);){
            worker.gotoUrl("http://www.bing.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.gotoLastPage("function extensions");
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void searchAsk() {
        try (Worker worker = Worker.create(DriverType.Firefox);){
            worker.gotoUrl("http://www.ask.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.gotoLastPage("function extensions");
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void searchAOL() {
        try (Worker worker = Worker.create(DriverType.Edge);){
            worker.gotoUrl("http://www.aol.com");
            SearchScreen searchScreen = worker.getScreen(SearchScreen.class);

            searchScreen.gotoLastPage("function extensions");
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
            searchScreen.gotoLastPage("function extensions");
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
        List<Worker> workers = Worker.createAll(2, DriverType.Chrome, DriverType.Edge, DriverType.Firefox, DriverType.Opera, DriverType.Chrome);

        try {
            Executor.runParallel(5,
                    () -> runSearch(workers, 0, "http://www.baidu.com"),
                    () -> runSearch(workers, 1, "http://www.bing.com"),
                    () -> runSearch(workers, 2, "http://www.aol.com"),
                    () -> runSearch(workers, 3, "http://www.ask.com"),
                    () -> runSearch(workers, 4, "http://www.google.com")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}