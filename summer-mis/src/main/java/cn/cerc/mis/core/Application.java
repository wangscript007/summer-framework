package cn.cerc.mis.core;

import cn.cerc.core.IHandle;
import cn.cerc.core.SupportHandle;
import cn.cerc.db.core.IAppConfig;
import cn.cerc.db.core.ServerConfig;
import cn.cerc.mis.language.Language;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Slf4j
public class Application {
    // Tomcat JSESSION.ID
    public static final String sessionId = "sessionId";
    // token id
    public static final String token = "ID";
    // user id
    public static final String userId = "UserID";
    public static final String userCode = "UserCode";
    public static final String userName = "UserName";
    public static final String roleCode = "RoleCode";
    public static final String bookNo = "BookNo";
    public static final String deviceLanguage = "language";
    // 签核代理用户列表，代理多个用户以半角逗号隔开
    public static final String ProxyUsers = "ProxyUsers";
    // 客户端代码
    public static final String clientIP = "clientIP";
    // 本地会话登录时间
    public static final String loginTime = "loginTime";
    // 浏览器通用客户设备Id
    public static final String webclient = "webclient";

    // 默认界面语言版本
    // FIXME: 2019/12/7 此处应改为从函数，并从配置文件中读取默认的语言类型
    public static final String App_Language = Language.zh_CN; // 可选：cn/en

    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    public static void setContext(ApplicationContext applicationContext) {
        if (context != applicationContext) {
            if (context == null) {
            } else {
                log.warn("applicationContext overload!");
            }
            context = applicationContext;
        }
    }

    public static ApplicationContext get(ServletContext servletContext) {
        setContext(WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext));
        return context;
    }

    public static ApplicationContext get(ServletRequest request) {
        setContext(WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext()));
        return context;
    }

    public static <T> T getBean(String beanId, Class<T> requiredType) {
        return context.getBean(beanId, requiredType);
    }

    public static <T> T getBean(Class<T> requiredType, String... beans) {
        for (String key : beans) {
            if (!context.containsBean(key)) {
                continue;
            }
            return context.getBean(key, requiredType);
        }
        return null;
    }

    public static <T> T getBean(Class<T> requiredType) {
        String[] items = requiredType.getName().split("\\.");
        String itemId = items[items.length - 1];

        String beanId;
        if (itemId.substring(0, 2).toUpperCase().equals(itemId.substring(0, 2))) {
            beanId = itemId;
        } else {
            beanId = itemId.substring(0, 1).toLowerCase() + itemId.substring(1);
        }

        return context.getBean(beanId, requiredType);
    }

    public static IHandle getHandle() {
        return getBean(IHandle.class, "AppHandle", "handle", "handleDefault");
    }

    public static IAppConfig getAppConfig() {
        return getBean(IAppConfig.class, "AppConfig", "appConfig", "appConfigDefault");
    }

    @Deprecated // 请改使用 getBean
    public static <T> T get(IHandle handle, Class<T> requiredType) {
        return getBean(handle, requiredType);
    }

    public static <T> T getBean(IHandle handle, Class<T> requiredType) {
        T bean = getBean(requiredType);
        if (bean != null && handle != null) {
            if (bean instanceof SupportHandle) {
                ((SupportHandle) bean).init(handle);
            }
        }
        return bean;
    }

    public static IService getService(IHandle handle, String serviceCode) {
        IService bean = context.getBean(serviceCode, IService.class);
        if (bean != null && handle != null) {
            bean.init(handle);
        }
        return bean;
    }

    public static IPassport getPassport(IHandle handle) {
        IPassport bean = getBean(IPassport.class, "passport", "passportDefault");
        if (bean != null && handle != null) {
            bean.setHandle(handle);
        }
        return bean;
    }

    public static IForm getForm(HttpServletRequest req, HttpServletResponse resp, String formId) {
        if (formId == null || "".equals(formId) || "service".equals(formId)) {
            return null;
        }

        setContext(WebApplicationContextUtils.getRequiredWebApplicationContext(req.getServletContext()));

        if (!context.containsBean(formId)) {
            throw new RuntimeException(String.format("form %s not find!", formId));
        }

        IForm form = context.getBean(formId, IForm.class);
        if (form != null) {
            form.setRequest(req);
            form.setResponse(resp);
        }

        return form;
    }

    public static String getLanguage() {
        String lang = ServerConfig.getInstance().getProperty(deviceLanguage);
        if (lang == null || "".equals(lang) || App_Language.equals(lang)) {
            return App_Language;
        } else if (Language.en_US.equals(lang)) {
            return lang;
        } else {
            throw new RuntimeException("not support language: " + lang);
        }
    }

}
