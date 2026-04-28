package com.g4.odontohub.steps;

import io.cucumber.java.Before;

public class Hooks {

    @Before
    public void setUp() {
        SharedTestServices.initialize();
    }
}
