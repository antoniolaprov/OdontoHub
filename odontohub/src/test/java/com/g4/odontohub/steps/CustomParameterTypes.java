package com.g4.odontohub.steps;

import io.cucumber.java.ParameterType;

public class CustomParameterTypes {

    @ParameterType("R\\$-?[\\d.]+,[\\d]{2}")
    public double dinheiro(String value) {
        return Double.parseDouble(
                value.replace("R$", "").replace(".", "").replace(",", "."));
    }
}
