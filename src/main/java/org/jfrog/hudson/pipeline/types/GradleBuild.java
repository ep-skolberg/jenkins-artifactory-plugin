package org.jfrog.hudson.pipeline.types;

import org.codehaus.jackson.map.ObjectMapper;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jfrog.hudson.pipeline.types.buildInfo.BuildInfo;
import org.jfrog.hudson.pipeline.types.deployers.GradleDeployer;
import org.jfrog.hudson.pipeline.types.resolvers.GradleResolver;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Tamirh on 04/08/2016.
 */
public class GradleBuild implements Serializable {
    private CpsScript cpsScript;
    private GradleDeployer deployer = new GradleDeployer();;
    private GradleResolver resolver = new GradleResolver();;
    private String tool = "";
    private boolean useWrapper;

    public GradleBuild() {
    }

    public void setCpsScript(CpsScript cpsScript) {
        this.cpsScript = cpsScript;
    }

    @Whitelisted
    public GradleDeployer getDeployer() {
        return deployer;
    }

    @Whitelisted
    public GradleResolver getResolver() {
        return resolver;
    }

    @Whitelisted
    public String getTool() {
        return tool;
    }

    @Whitelisted
    public void setTool(String tool) {
        this.tool = tool;
    }

    @Whitelisted
    public boolean isUseWrapper() {
        return useWrapper;
    }

    @Whitelisted
    public void setUseWrapper(boolean useWrapper) {
        this.useWrapper = useWrapper;
    }

    @Whitelisted
    public BuildInfo run(Map<String, Object> args) {
        if (!args.containsKey("tasks") || !args.containsKey("buildFile")|| !args.containsKey("rooBuildScriptDir")) {
            throw new IllegalArgumentException("rooBuildScriptDir, buildFile and tasks are mandatory arguments.");
        }
        Map<String, Object> stepVariables = getRunArguments((String) args.get("buildFile"), (String) args.get("tasks"), (String) args.get("switches"), (String) args.get("rooBuildScriptDir"), (BuildInfo) args.get("buildInfo"));
        BuildInfo build = (BuildInfo) cpsScript.invokeMethod("ArtifactoryGradleBuild", stepVariables);
        return build;
    }

    @Whitelisted
    public void resolver(Map<String, Object> resolverArguments) throws Exception {
        Set<String> resolverArgumentsSet = resolverArguments.keySet();
        List<String> keysAsList = Arrays.asList(new String[]{"releaseRepo", "server"});
        if (!keysAsList.containsAll(resolverArgumentsSet)) {
            throw new IllegalArgumentException("Only the following arguments are allowed: " + keysAsList.toString());
        }

        // We don't want to handle the deserialization of the ArtifactoryServer.
        // Instead we will remove it and later on set it on the deployer object.
        Object server = resolverArguments.remove("server");
        final ObjectMapper mapper = new ObjectMapper();
        this.resolver = mapper.convertValue(resolverArguments, GradleResolver.class);
        if (server != null) {
            this.resolver.setServer((ArtifactoryServer) server);
        }
    }

    @Whitelisted
    public void deployer(Map<String, Object> deployerArguments) throws Exception {
        Set<String> resolverArgumentsSet = deployerArguments.keySet();
        List<String> keysAsList = Arrays.asList(new String[]{"releaseRepo", "server", "deployArtifacts", "includeEnvVars", "usesPlugin", "deployMaven", "deployIvy", "ivyPattern", "artifactPattern"});
        if (!keysAsList.containsAll(resolverArgumentsSet)) {
            throw new IllegalArgumentException("Only the following arguments are allowed: " + keysAsList.toString());
        }

        // We don't want to handle the deserialization of the ArtifactoryServer.
        // Instead we will remove it and later on set it on the deployer object.
        Object server = deployerArguments.remove("server");
        final ObjectMapper mapper = new ObjectMapper();
        this.deployer = mapper.convertValue(deployerArguments, GradleDeployer.class);
        if (server != null) {
            this.deployer.setServer((ArtifactoryServer) server);
        }
    }

    private Map<String, Object> getRunArguments(String buildFile, String tasks, String switches, String rooBuildScriptDir, BuildInfo buildInfo) {
        Map<String, Object> stepVariables = new LinkedHashMap<String, Object>();
        stepVariables.put("gradleBuild", this);
        stepVariables.put("tool", tool);
        stepVariables.put("rooBuildScriptDir", rooBuildScriptDir);
        stepVariables.put("buildFile", buildFile);
        stepVariables.put("tasks", tasks);
        stepVariables.put("switches", switches);
        stepVariables.put("useWrapper", useWrapper);
        stepVariables.put("buildInfo", buildInfo);
        return stepVariables;
    }
}
