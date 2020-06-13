package cn.cerc.mis.core;

import cn.cerc.core.IHandle;
import cn.cerc.db.core.IAppConfig;
import cn.cerc.ui.core.UrlRecord;
import org.apache.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Deprecated // 请改使用 StartAppDefault
public class StartApp implements Filter {
    private static final Logger log = Logger.getLogger(StartApp.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        StringBuffer builder = req.getRequestURL();
        UrlRecord url = new UrlRecord();
        url.setSite(builder.toString());
        Map<String, String[]> items = req.getParameterMap();
        for (String key : items.keySet()) {
            String[] values = items.get(key);
            for (String value : values) {
                url.putParam(key, value);
            }
        }
        log.info("url " + url.getUrl());

        String uri = req.getRequestURI();
        Application.get(req);

        // 处理默认首页问题
        if (uri.equals("/")) {
            if (req.getParameter(ClientDevice.deviceId_key) != null)
                req.getSession().setAttribute(ClientDevice.deviceId_key, req.getParameter(ClientDevice.deviceId_key));
            if (req.getParameter(ClientDevice.deviceType_key) != null)
                req.getSession().setAttribute(ClientDevice.deviceType_key,
                        req.getParameter(ClientDevice.deviceType_key));

            IAppConfig conf = Application.getAppConfig();
            resp.sendRedirect(String.format("/%s/%s", conf.getPathForms(), conf.getFormWelcome()));
            return;
        } else if (uri.equals("/MobileConfig") || uri.equals("/mobileConfig")) {
            if (req.getParameter(ClientDevice.deviceId_key) != null)
                req.getSession().setAttribute(ClientDevice.deviceId_key, req.getParameter(ClientDevice.deviceId_key));
            if (req.getParameter(ClientDevice.deviceType_key) != null)
                req.getSession().setAttribute(ClientDevice.deviceType_key,
                        req.getParameter(ClientDevice.deviceType_key));
            try {
                IForm form;
                if (Application.get(req).containsBean("mobileConfig"))
                    form = Application.getBean("mobileConfig", IForm.class);
                else
                    form = Application.getBean("MobileConfig", IForm.class);
                form.setRequest((HttpServletRequest) request);
                form.setResponse((HttpServletResponse) response);

                IHandle handle = Application.getHandle();
                handle.setProperty(Application.sessionId, req.getSession().getId());
                form.setHandle(handle);
                IPage page = form.execute();
                page.execute();
            } catch (Exception e) {
                resp.getWriter().print(e.getMessage());
            }
            return;
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}