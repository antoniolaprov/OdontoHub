package com.g4.odontohub;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/F01_agendamento.feature")
@SelectClasspathResource("features/F02_anamnese.feature")
@SelectClasspathResource("features/F03_plano_tratamento.feature")
@SelectClasspathResource("features/F04_fluxo_caixa.feature")
@SelectClasspathResource("features/F05_estoque.feature")
@SelectClasspathResource("features/F06_esterilizacao.feature")
@SelectClasspathResource("features/F07_relatorios.feature")
@SelectClasspathResource("features/F08_prescricao.feature")
@SelectClasspathResource("features/F10_followup.feature")
@SelectClasspathResource("features/F11_churn.feature")
@SelectClasspathResource("features/F12_equipe.feature")
@SelectClasspathResource("features/F13_confirmacao_lembretes.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.g4.odontohub.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, summary")
public class CucumberRunnerTest {}
