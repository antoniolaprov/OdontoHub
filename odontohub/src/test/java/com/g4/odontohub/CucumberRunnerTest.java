package com.g4.odontohub;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/F02_anamnese.feature")
@SelectClasspathResource("features/F04_fluxo_caixa.feature")
@SelectClasspathResource("features/F05_estoque.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.g4.odontohub.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, summary")
public class CucumberRunnerTest {}
