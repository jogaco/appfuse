<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${basepackage}.webapp.pages;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.corelib.components.EventLink;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.PersistenceConstants;

<#if genericcore>
import ${appfusepackage}.service.GenericManager;
<#else>
import ${basepackage}.service.${pojo.shortName}Manager;
</#if>
import ${pojo.packageName}.${pojo.shortName};

import org.slf4j.Logger;

import java.util.List;

public class ${pojo.shortName}Form {
    @Inject
    private Logger log;

    @Inject
    @Service("${pojoNameLower}Manager")
<#if genericcore>
    private GenericManager<${pojo.shortName}, ${identifierType}> ${pojoNameLower}Manager;
<#else>
    private ${pojo.shortName}Manager ${pojoNameLower}Manager;
</#if>

    @Inject
    private AlertManager alertManager;

    @Inject
    private Messages messages;

    @Persist
    private ${pojo.shortName} ${pojoNameLower};

    public ${pojo.shortName} get${pojo.shortName}() {
        return ${pojoNameLower};
    }

    /**
     * Allows setting ${pojoNameLower} object from another class (i.e. ${pojo.shortName}List)
     *
     * @param ${pojoNameLower} an initialized instance
     */
    public void set${pojo.shortName}(${pojo.shortName} ${pojoNameLower}) {
        this.${pojoNameLower} = ${pojoNameLower};
    }

    @InjectPage
    private ${pojo.shortName}List ${pojoNameLower}List;

    @Component(id = "${pojoNameLower}Form")
    private Form form;

    private boolean cancel;
    private boolean delete;

    void onValidateForm() {
        if (!delete && !cancel) {
            // manually validate required fields or annotate the ${pojo.shortName} object
            /*if (foo.getProperty() == null || user.getProperty().trim().equals("")) {
                form.recordError("Property is a required field.");
            }*/
        }
    }

    void onActivate(${identifierType} ${pojo.identifierProperty.name}) {
        if (${pojo.identifierProperty.name} != null) {
            ${pojoNameLower} = ${pojoNameLower}Manager.get(${pojo.identifierProperty.name});
        }
    }

    Object onSuccess() {
        if (delete) return onDelete();
        if (cancel) return onCancel();

        log.debug("Saving ${pojoNameLower}...");

        boolean isNew = (get${pojo.shortName}().${getIdMethodName}() == null);

        ${pojoNameLower}Manager.save(${pojoNameLower});

        String key = (isNew) ? "${pojoNameLower}.added" : "${pojoNameLower}.updated";
        alertManager.alert(Duration.TRANSIENT, Severity.INFO,  messages.get(key));

        if (isNew) {
            return ${pojoNameLower}List;
        } else {
            return this;
        }
    }

    void onSelectedFromDelete() {
        log.debug("Deleting ${pojoNameLower}...");
        delete = true;
    }

    void onSelectedFromCancel() {
        log.debug("Cancelling form...");
        cancel = true;
    }

    Object onDelete() {
        ${pojoNameLower}Manager.remove(${pojoNameLower}.${getIdMethodName}());
        alertManager.alert(Duration.TRANSIENT, Severity.INFO, messages.format("${pojoNameLower}.deleted"));
        return ${pojoNameLower}List;
    }

    Object onCancel() {
        return ${pojoNameLower}List;
    }
}