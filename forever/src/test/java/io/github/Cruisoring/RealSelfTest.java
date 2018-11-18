package io.github.Cruisoring;

import io.github.Cruisoring.helpers.*;
import io.github.Cruisoring.jt2345.CheciScreen;
import io.github.Cruisoring.jt2345.DetailScreen;
import io.github.Cruisoring.wrappers.UITable;
import io.github.cruisoring.Functions;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Test;
import realSelf.FindScreen;
import realSelf.SearchResultsScreen;
import realSelf.SpecialtyScreen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RealSelfTest {

    public static final String bookPath = "C:/working/RealSelf.xlsx";
    public static final String startUrl = "https://www.realself.com/find/";
    public static final URL baseUrl = (URL)Functions.Default.apply(() -> new URL("https://www.realself.com/find/"));

    static final Worker master;

    static {
        master = Worker.getAvailable();
    }

    FindScreen findScreen = master.getScreen(FindScreen.class);
    SpecialtyScreen specialtyScreen = master.getScreen(SpecialtyScreen.class);
    SearchResultsScreen searchResultsScreen = master.getScreen(SearchResultsScreen.class);

    @AfterClass
    public static void afterClass() throws Exception {
        if(master != null){
            master.close();
        }
    }

    @Test
    public void openFirst(){
        master.gotoUrl(baseUrl);
        List<String> allPlaces = findScreen.findPlaces();

    }
}
