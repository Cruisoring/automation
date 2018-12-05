package io.github.Cruisoring.interfaces;

import io.github.Cruisoring.workers.Worker;
import org.openqa.selenium.SearchContext;

public interface WorkingContext extends SearchContext{
    Worker getWorker();
    void invalidate();
}
