package edu.rpi.tw.twks.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

import java.util.HashMap;
import java.util.Map;

public class GlobalArgs {
    @DynamicParameter(names = "-D", description = "library configuration, overrides -c and system properties")
    Map<String, String> configuration = new HashMap<>();

    @Parameter(names = {"-c"}, description = "library configuration file path in .properties format")
    String configurationFilePath;

    @Parameter(names = {"-h", "--help"})
    boolean help = false;

    @Parameter(names = {"--report-metrics"})
    boolean reportMetrics = false;

    @Parameter(names = {"-v", "--version"})
    boolean version = false;
}
