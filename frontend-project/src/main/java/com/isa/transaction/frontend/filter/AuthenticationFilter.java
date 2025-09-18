package com.isa.transaction.frontend.filter;

import com.isa.transaction.frontend.bean.LoginBean;

import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/transacciones.xhtml"})
public class AuthenticationFilter implements Filter {

    @Inject
    private LoginBean loginBean;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        // Check if the user is logged in using the injected bean
        if (loginBean == null || !loginBean.isLoggedIn()) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        } else {
            // User is logged in, so just continue the request
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Not used
    }

    @Override
    public void destroy() {
        // Not used
    }
}