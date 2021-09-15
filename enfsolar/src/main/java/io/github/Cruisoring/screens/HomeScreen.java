package io.github.Cruisoring.screens;


import io.github.Cruisoring.components.Country;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.cruisoring.Lazy;
import io.github.cruisoring.tuple.Tuple;
import io.github.cruisoring.tuple.Tuple3;
import org.openqa.selenium.By;

import java.util.Map;
import java.util.stream.Collectors;

public class HomeScreen extends Screen {

    public final UICollection<Country> allCountries;
    private final Lazy<Map<String, Tuple3<Country, String, Integer>>> countryMap;

    protected HomeScreen(Worker worker) {
        super(worker);
        allCountries = new UICollection<Country>(this, By.cssSelector("div.mk-body"), 0, Country.class, Country.countryBy);

        countryMap = new Lazy<Map<String, Tuple3<Country, String, Integer>>>(() -> allCountries.getChildren().stream()
                .collect(Collectors.toMap(
                        c -> c.getName(),
                        c -> Tuple.create(c, c.getLink(), c.getCount())
                ))
        );
    }

    public Country getCountry(String name){
        Map<String, Tuple3<Country, String, Integer>> map = countryMap.getValue();
        if(!map.containsKey(name))
            return null;

        return map.get(name).getFirst();
    }

    public String getLink(String name){
        Map<String, Tuple3<Country, String, Integer>> map = countryMap.getValue();
        if(!map.containsKey(name))
            return null;

        return map.get(name).getSecond();
    }

    public int getCount(String name){
        Map<String, Tuple3<Country, String, Integer>> map = countryMap.getValue();
        if(!map.containsKey(name))
            return -1;

        return map.get(name).getThird();
    }
}
