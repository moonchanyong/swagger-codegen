package io.swagger.codegen.languages;

import io.swagger.codegen.*;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmeriaServerCodegen extends AbstractJavaCodegen {
    static Logger LOGGER = LoggerFactory.getLogger(ArmeriaServerCodegen.class);

    public static final String PROJECT_NAME = "projectName";
    public static final String JSON = "json";
    protected String basePackage = "io.swagger";
    protected boolean json = true;

    // Override field
    public static final String SCM_URL_DESC = "SCM URL in generated build.gradle";


    public void setJson(boolean json) {
        this.json = json;
    }
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    public String getName() {
        return "armeria";
    }

    private final String[] REMOVE_OPTIONS = new String[] { "hideGenerationTimestamp", DISABLE_HTML_ESCAPING, JAVA8_MODE, DATE_LIBRARY };

    public String getHelp() {
        return "Generates a armeria server.";
    }

    public ArmeriaServerCodegen() {
        // Use Instant to default DateLib
        dateLibrary = "java8-instant";

        // Set cliOptions
        Set<String> removedOptions = new HashSet<>(Arrays.asList(REMOVE_OPTIONS));
        cliOptions.add(CliOption.newBoolean(JSON, "server title name or client service name"));

        List<CliOption> _cliOptions = new ArrayList<>();
        for (CliOption cliOption : cliOptions) {
             if (!removedOptions.contains(cliOption.getOpt())) {
                 cliOption.setDescription(cliOption.getDescription().replace("pom.xml", "build.gradle"));
                 _cliOptions.add(cliOption);
             }
        }
        cliOptions = _cliOptions;

        additionalProperties.put(JSON, false);
        additionalProperties.put(JAVA8_MODE, false);
        java8Mode = true;

        outputFolder = "generated-code" + File.separator + "armeria";
        embeddedTemplateDir = templateDir = "armeria-server";
        apiPackage = basePackage + ".service";
        modelPackage = basePackage + ".model";

        // Mapping Armeira Annotatation
        importMapping.put("Get", "com.linecorp.armeria.server.annotation.Get");
        importMapping.put("Post", "com.linecorp.armeria.server.annotation.Post");
        importMapping.put("Put", "com.linecorp.armeria.server.annotation.Put");
        importMapping.put("Delete", "com.linecorp.armeria.server.annotation.Delete");
        importMapping.put("Options", "com.linecorp.armeria.server.annotation.Options");
        importMapping.put("Head", "com.linecorp.armeria.server.annotation.Head");
        importMapping.put("Patch", "com.linecorp.armeria.server.annotation.Patch");
        importMapping.put("Trace", "com.linecorp.armeria.server.annotation.Trace");
   }

    @Override
    public CodegenParameter fromParameter(Parameter param, Set<String> imports) {
        CodegenParameter _param = super.fromParameter(param, imports);
        // Don't use java.io.file
        imports.remove("File");
        return _param;
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);
        op.httpMethod = camelize(httpMethod.toLowerCase());
        return op;
    }

    // TODO: remove after added multipart content support and fixing template
    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> obj = super.postProcessOperations(objs);
        Map<String, Object> operations = (Map<String, Object>) obj.get("operations");
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        Set<String> libs = new HashSet<>();

        // Libs by default imported
        libs.add(importMapping.get("List"));
        libs.add("javax.validation.constraints.NotNull");
        libs.add("com.linecorp.armeria.common.HttpStatus");
        libs.add("com.linecorp.armeria.common.HttpResponse");
        libs.add("com.linecorp.armeria.common.HttpRequest");
        libs.add("com.linecorp.armeria.server.annotation.Consumes");
        libs.add("com.linecorp.armeria.server.annotation.Produces");
        libs.add("com.linecorp.armeria.server.annotation.Description");
        libs.add("com.linecorp.armeria.server.annotation.Header");
        libs.add("com.linecorp.armeria.server.annotation.Param");

        if (operations != null) {
            String annotationPackage = "com.linecorp.armeria.server.annotation.";
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation operation : ops) {
                libs.add(importMapping.get(operation.httpMethod));

                boolean isMultiPart = false;
                if (operation.consumes != null) {
                    for (Map<String, String> consume : operation.consumes) {
                        String mediaType = consume.get("mediaType");
                        if (mediaType.equals("multipart/form-data")) {
                            isMultiPart = true;
                        }

                        switch (mediaType) {
                            case "application/binary":
                                consume.put("annotation", "@ConsumesBinary");
                                libs.add(annotationPackage + "ConsumesBinary");
                                break;
                            case "application/json":
                                consume.put("annotation", "@ConsumesJson");
                                libs.add(annotationPackage + "ConsumesJson");
                                break;
                            case "application/octet-stream":
                                consume.put("annotation", "@ConsumesOctetStream");
                                libs.add(annotationPackage + "ConsumesOctetStream");
                                break;
                            case "text/plain":
                                consume.put("annotation", "@ConsumesText");
                                libs.add(annotationPackage + "ConsumesText");
                                break;
                            default:
                                consume.put("annotation", "@Consumes(\"" + mediaType + "\")");
                        }
                    }
                }

                if (operation.produces != null) {
                    for (Map<String, String> produce : operation.produces) {
                        String mediaType = produce.get("mediaType");
                        switch (mediaType) {
                            case "application/binary":
                                produce.put("annotation", "@ProducesBinary");
                                libs.add(annotationPackage + "ProducesBinary");
                                break;
                            case "application/json":
                                produce.put("annotation", "@ProducesJson");
                                libs.add(annotationPackage + "ProducesJson");
                                break;
                            case "application/octet-stream":
                                produce.put("annotation", "@ProducesOctetStream");
                                libs.add(annotationPackage + "ProducesOctetStream");
                                break;
                            case "text/plain":
                                produce.put("annotation", "@ProducesText");
                                libs.add(annotationPackage + "ProducesText");
                                break;
                            case "text/event-stream":
                                produce.put("annotation", "@ProducesEventStream");
                                libs.add(annotationPackage + "ProducesEventStream");
                                break;
                            case "application/json-seq":
                                produce.put("annotation", "@ProducesJsonSequences");
                                libs.add(annotationPackage + "ProducesJsonSequences");
                            break;
                            default:
                                produce.put("annotation", "@Produces(\"" + mediaType + "\")");
                        }
                    }
                }
                // labeling multipart/form-data
                if (isMultiPart && !operation.allParams.isEmpty()) {
                    boolean hasFormParam = false;
                    CodegenParameter formParam = null;
                    List <CodegenParameter> params = new ArrayList<>();
                    for (CodegenParameter param : operation.allParams) {
                        if (hasFormParam && formParam != null && !param.isFormParam) {
                            formParam.hasMore = true;
                        } else if (!hasFormParam && param.isFormParam) {
                            hasFormParam = true;
                            param.vendorExtensions.put("x-is-support-multipart", true);
                            param.paramName = "multipartForm";
                            param.hasMore = false;
                            params.add(formParam = param);
                        }

                        if (!param.isFormParam) {
                            params.add(param);
                        }
                    }
                    operation.allParams = params;
                }
            }

            for (Iterator<String> itr = libs.iterator(); itr.hasNext();) {
                String lib = itr.next();
                Map<String, String> item = new HashMap<>();
                item.put("import", lib);
                imports.add(item);
            }
        }
        return obj;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(JSON)) {
            setJson(Boolean.valueOf(additionalProperties.get(JSON).toString()));
            additionalProperties.put(JSON, Boolean.valueOf(additionalProperties.get(JSON).toString()));
        }

        supportingFiles.add(new SupportingFile("Main.mustache", sourceFolder + File.separator + invokerPackage.replace(".", File.separator), "Main.java"));
        supportingFiles.add(new SupportingFile("build.gradle.mustache", "", "build.gradle"));

        // does not support auto-generated markdown doc at the moment.
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");
        apiTestTemplateFiles.remove("api_test.mustache");
    }

    @Override
    public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);
        codegenModel.imports.remove("ApiModel");

        if (!codegenModel.isEnum && codegenModel.hasVars) {
            codegenModel.imports.add("Objects");
        }

        if (json) {
            codegenModel.imports.add("JsonProperty");
            codegenModel.imports.add("JsonValue");
        }

        return codegenModel;
    }

    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
       return objs;
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        Map<String, Object> _objs = super.postProcessModels(objs);
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        Map<String, String> item = new HashMap<>();
        item.put("import", "com.linecorp.armeria.server.annotation.Description");
        imports.add(item);
        return _objs;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        model.imports.remove("ApiModel");
        model.imports.remove("ApiModelProperty");
    }
}
