package com.least.automation.interfaces;

import com.least.automation.helpers.Worker;
import org.openqa.selenium.SearchContext;

public interface WorkingContext extends SearchContext{
    Worker getWorker();
    void invalidate();
}
