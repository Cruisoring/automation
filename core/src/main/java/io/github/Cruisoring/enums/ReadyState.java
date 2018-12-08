package io.github.Cruisoring.enums;


import io.github.Cruisoring.helpers.EnumHelper;

public enum ReadyState {
    uninitialized   // - Has not started loading yet
    , loading       // - Is loading
    , loaded        // - Has been loaded
    , interactive   // - Has loaded enough and the user can interact with it
    , complete      // - Fully loaded
    , unknown;

    public static ReadyState fromString(String value) {
        return EnumHelper.toEnum(value, unknown);
    }

    public static boolean isReady(ReadyState state){
        return state.equals(loaded) || state.equals(complete);
    }

    public boolean isReady(){
        return isReady(this);
    }
}

