package org.appfuse.webapp.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ValidationDecorator;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.upload.services.UploadSymbols;
import org.appfuse.model.Role;
import org.appfuse.model.User;
import org.appfuse.service.RoleManager;
import org.appfuse.service.UserManager;
import org.appfuse.webapp.AppFuseSymbolConstants;
import org.appfuse.webapp.data.FileData;
import org.appfuse.webapp.services.impl.*;
import org.appfuse.webapp.services.javascript.BootstrapJavaScriptStack;
import org.slf4j.Logger;

import java.io.IOException;


/**
 * Application global configurations
 *
 * @author Serge Eby
 * @version $Id: AppModule.java 5 2008-08-30 09:59:21Z serge.eby $
 */
public class AppModule {

    public static void bind(ServiceBinder binder) {
        binder.bind(SecurityContext.class, SpringSecurityContext.class);
        binder.bind(EmailService.class, EmailServiceImpl.class);
    }

//
//    public static MenuModelSource buildMenuModelSource(Logger logger, @Inject SecurityContext securityContext) {
//        return new MenuModelSourceImpl(logger, securityContext);
//    }

    //@Contribute(SymbolProvider.class)
    //@ApplicationDefaults
    //public static void enableDevelopmentMode(MappedConfiguration<String, Object> configuration) {
    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration) {
        configuration.add(SymbolConstants.SUPPORTED_LOCALES,
                "de,en,es,fr,it,ko,nl,no,pt_BR,pt,tr,zh_CN,zh_TW,en_US");


        // Turn off GZip Compression since it causes issues with SiteMesh
        configuration.add(SymbolConstants.GZIP_COMPRESSION_ENABLED, "false");

        // The factory default is true but during the early stages of an application
        // overriding to false is a good idea. In addition, this is often overridden
        // on the command line as -Dtapestry.production-mode=false
        configuration.add(SymbolConstants.PRODUCTION_MODE, "false");

        // Maximum upload size is 2MB (size is in bytes)
        configuration.add(UploadSymbols.FILESIZE_MAX, "2048000");

        // Spring Security
        configuration.add(AppFuseSymbolConstants.SECURITY_URL, "/j_security_check");

    }


    public static CountryService buildCountryService(final Logger logger, final ThreadLocale threadLocale) {
        return new CountryServiceImpl(logger, threadLocale);
    }

    @Contribute(ValueEncoderSource.class)
    public static void provideEncoders(
            MappedConfiguration<Class, ValueEncoderFactory> configuration,
            UserManager userManager,
            RoleManager roleManager) {

        contributeEncoder(configuration, User.class, new UserEncoder(userManager));
        contributeEncoder(configuration, Role.class, new RoleEncoder(roleManager));
        contributeEncoder(configuration, FileData.class, new FileDataEncoder());


    }

    public static void contributeJavaScriptStackSource(MappedConfiguration<String, JavaScriptStack> configuration) {
        configuration.addInstance(AppFuseSymbolConstants.BOOTSTRAP_STACK, BootstrapJavaScriptStack.class);
    }

    private static <T> void contributeEncoder(MappedConfiguration<Class, ValueEncoderFactory> configuration,
                                              Class<T> clazz, final ValueEncoder<T> encoder) {

        ValueEncoderFactory<T> factory = new ValueEncoderFactory<T>() {

            public ValueEncoder<T> create(Class<T> clazz) {
                return encoder;
            }
        };

        configuration.add(clazz, factory);
    }


//    @Contribute(BeanBlockOverrideSource.class)
//    public static void providePropertyBlocks(
//            Configuration<BeanBlockContribution> configuration) {
//
//        addEditBlock(configuration, DataTypeConstants.TEXT);
//        addEditBlock(configuration, DataTypeConstants.NUMBER);
//        addEditBlock(configuration, DataTypeConstants.ENUM);
//        addEditBlock(configuration, DataTypeConstants.BOOLEAN);
//        addEditBlock(configuration, DataTypeConstants.DATE);
//        addEditBlock(configuration, DataTypeConstants.PASSWORD);
//        addEditBlock(configuration, "hashedpassword");
//        addEditBlock(configuration, DataTypeConstants.CALENDAR);
//        addEditBlock(configuration, DataTypeConstants.LONG_TEXT);
//
//    }
//
//    private static void addEditBlock(Configuration<BeanBlockContribution> configuration, String dataType) {
//        configuration.add(new EditBlockContribution(dataType, "AppPropertyEditBlocks", dataType));
//    }
//
//
//    private static void addDisplayBlock(Configuration<BeanBlockContribution> configuration, String dataType) {
//        configuration.add(new DisplayBlockContribution(dataType, "AppPropertyDisplayBlocks", dataType));
//    }


    public void contributeMarkupRenderer(OrderedConfiguration<MarkupRendererFilter> configuration,
                                         final Environment environment

    ) {
        MarkupRendererFilter bootstrapValidationDecorator = new MarkupRendererFilter() {

            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer) {


                environment.push(ValidationDecorator.class, new BootstrapValidationDecorator(environment, writer));

                renderer.renderMarkup(writer);

                environment.pop(ValidationDecorator.class);
            }
        };
        //configuration.add("BootstrapValidationDecorator", bootstrapValidationDecorator, "after:ValidationDecorator");
        configuration.override("ValidationDecorator", bootstrapValidationDecorator);

    }


    /**
     * Decorate Error page
     *
     * @param logger
     * @param renderer
     * @param componentSource
     * @param productionMode
     * @param service
     * @return
     */
    public RequestExceptionHandler decorateRequestExceptionHandler(
            final Logger logger,
            final ResponseRenderer renderer,
            final ComponentSource componentSource,
            @Symbol(SymbolConstants.PRODUCTION_MODE)
            boolean productionMode,
            Object service) {
        if (!productionMode) {
            return null;
        }

        return new RequestExceptionHandler() {
            public void handleRequestException(Throwable exception) throws IOException {
                logger.error("Unexpected runtime exception: " + exception.getMessage(), exception);
                ExceptionReporter error = (ExceptionReporter) componentSource.getPage("Error");
                error.reportException(exception);
                renderer.renderPageMarkupResponse("Error");
            }
        };
    }


//    public static void contributeSymbolSource(OrderedConfiguration<SymbolProvider> providers) {
//        providers.add("springSecurity", new ClasspathResourceSymbolProvider("security.properties"));
//    }

}
