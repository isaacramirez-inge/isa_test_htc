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

        // Verifica si el usuario ha iniciado sesion utilizando el bean inyectado
        if (loginBean == null || !loginBean.isLoggedIn()) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        } else {
            // El usuario ha iniciado sesion, por lo que simplemente continua con la solicitud
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No utilizado
    }

    @Override
    public void destroy() {
        // No utilizado
    }
}
