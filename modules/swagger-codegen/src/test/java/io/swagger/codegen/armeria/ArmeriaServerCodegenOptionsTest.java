package io.swagger.codegen.armeria;

import io.swagger.codegen.AbstractOptionsTest;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.languages.ArmeriaServerCodegen;
import io.swagger.codegen.options.ArmeriaServerCodegenOptionsProvider;

import mockit.Expectations;
import mockit.Tested;

public class ArmeriaServerCodegenOptionsTest extends AbstractOptionsTest {

    @Tested
    private ArmeriaServerCodegen codegen;

    public ArmeriaServerCodegenOptionsTest() {
        super(new ArmeriaServerCodegenOptionsProvider());
    }

    @Override
    protected CodegenConfig getCodegenConfig() {
        return codegen;
    }

    @SuppressWarnings("unused")
    @Override
    protected void setExpectations() {
        // TODO: Complete options
        new Expectations(codegen) {{

        }};
    }
}

